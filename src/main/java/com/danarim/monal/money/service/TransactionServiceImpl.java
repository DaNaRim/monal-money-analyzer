package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.ActionDeniedException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.InternalServerException;
import com.danarim.monal.exceptions.ValidationCodes;
import com.danarim.monal.money.persistence.dao.TransactionDao;
import com.danarim.monal.money.persistence.model.CurrencyType;
import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.TransactionType;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateTransactionDto;
import com.danarim.monal.money.web.dto.UpdateTransactionDto;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

/**
 * Service for {@link Transaction Transaction}.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionCategoryService categoryService;
    private final WalletService walletService;
    private final TransactionDao transactionDao;

    /**
     * Dependency injection constructor.
     *
     * @param categoryService TransactionCategoryService
     * @param walletService   WalletService
     * @param transactionDao  TransactionDao
     */
    public TransactionServiceImpl(TransactionCategoryService categoryService,
                                  WalletService walletService,
                                  TransactionDao transactionDao
    ) {
        this.categoryService = categoryService;
        this.walletService = walletService;
        this.transactionDao = transactionDao;
    }

    /**
     * Checks if the transaction data is valid. Creates a new transaction and updates the wallet
     * balance.
     *
     * @param createTransactionDto DTO with transaction data
     * @param userId               logged in user ID
     *
     * @return created transaction
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public Transaction createTransaction(CreateTransactionDto createTransactionDto, long userId) {
        TransactionType categoryType =
                categoryService.getCategoryType(createTransactionDto.categoryId());

        Optional<Wallet> optionalWallet =
                walletService.getWalletForUpdate(createTransactionDto.walletId());

        validateCreateTransaction(createTransactionDto, userId, categoryType, optionalWallet);

        Wallet wallet = optionalWallet.get(); // Wallet is present because of validation

        double amount = roundTransactionAmount(
                createTransactionDto.amount(), wallet.getCurrency().getType()
        );
        Transaction result = transactionDao.save(new Transaction(
                createTransactionDto.description() == null
                        ? null
                        : createTransactionDto.description().trim().replaceAll("\\s+", " "),
                new Date(createTransactionDto.date().getTime()),
                amount,
                new TransactionCategory(createTransactionDto.categoryId()),
                wallet
        ));
        walletService.updateWalletBalance(
                wallet,
                categoryType == TransactionType.INCOME ? amount : -amount
        );
        return result;
    }

    /**
     * Gets all transactions between two dates for a wallet.
     *
     * @param from         date from
     * @param to           date to
     * @param walletId     wallet ID
     * @param loggedUserId logged in user ID
     *
     * @return list of transactions
     *
     * @throws AccessDeniedException if a user does not own the wallet
     * @throws BadRequestException   if date 'from' is after date 'to'
     */
    @Override
    public List<Transaction> getTransactionsBetweenDates(Date from,
                                                         Date to, long walletId,
                                                         long loggedUserId
    ) {
        if (!walletService.isUserWalletOwner(walletId, loggedUserId)) {
            throw new AccessDeniedException("User with ID %d is not the owner of wallet with ID %d"
                                                    .formatted(loggedUserId, walletId));
        }
        if (from.after(to)) {
            throw new BadRequestException("Date 'from' must be before date 'to'",
                                          ValidationCodes.TRANSACTION_DATE_FROM_AFTER_DATE_TO,
                                          null);
        }
        return transactionDao.getTransactionsByWalletIdAndDateBetween(walletId, from, to);
    }

    /**
     * Deletes a transaction if it exists and the user is the owner of the transaction.
     *
     * @param transactionId transaction ID
     * @param loggedUserId  logged in user ID
     *
     * @throws BadRequestException   if the transaction does not exist
     * @throws ActionDeniedException if the user is not the owner of the transaction
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteTransaction(long transactionId, long loggedUserId) {
        if (!transactionDao.existsById(transactionId)) {
            throw new BadRequestException(
                    "Transaction with ID %d does not exist.".formatted(transactionId),
                    ValidationCodes.TRANSACTION_NOT_FOUND,
                    null);
        }
        if (!transactionDao.isUserTransactionOwner(transactionId, loggedUserId)) {
            throw new ActionDeniedException(
                    "User with ID %d is not the owner of transaction with ID %d"
                            .formatted(loggedUserId, transactionId));
        }
        Transaction transaction = transactionDao.getById(transactionId);
        Optional<Wallet> walletForUpdate =
                walletService.getWalletForUpdate(transaction.getWallet().getId());

        if (walletForUpdate.isEmpty()) { // Should never happen
            throw new InternalServerException(
                    "Wallet with ID %d does not exist.".formatted(transaction.getWallet().getId())
            );
        }
        transactionDao.deleteById(transactionId);

        walletService.updateWalletBalance(walletForUpdate.get(),
                                          transaction.getCategory().getType()
                                                  == TransactionType.INCOME
                                                  ? -transaction.getAmount()
                                                  : transaction.getAmount());
    }

    /**
     * Updates a transaction and the wallet balance.
     *
     * @param transactionDto DTO with transaction data
     * @param loggedUserId   logged in user ID
     *
     * @return updated transaction
     */
    @Override
    @Transactional(rollbackOn = Exception.class)
    public Transaction updateTransaction(UpdateTransactionDto transactionDto, long loggedUserId) {
        TransactionType newCategoryType =
                categoryService.getCategoryType(transactionDto.categoryId());
        Optional<Wallet> optionalNewWallet =
                walletService.getWalletForUpdate(transactionDto.walletId());

        validateUpdateTransaction(transactionDto, loggedUserId, newCategoryType, optionalNewWallet);

        // Transaction is present because of validation
        Transaction transaction = transactionDao.findById(transactionDto.id()).get();

        // New wallet must have the same currency as the old wallet
        if (walletService.getWalletCurrency(transactionDto.walletId())
                != optionalNewWallet.get().getCurrency()) {
            throw new BadRequestException(
                    "Wallet with ID %d has different currency than transaction with ID %d"
                            .formatted(transactionDto.walletId(), transactionDto.id()),
                    ValidationCodes.TRANSACTION_WALLET_HAS_DIFFERENT_CURRENCY,
                    null);
        }
        // Wallet, category type or amount changed
        if (transaction.getWallet().getId() != transactionDto.walletId()
                || transaction.getCategory().getType() != newCategoryType
                || transaction.getAmount() != transactionDto.amount()) {
            updateWalletBallanceOnUpdate(transactionDto,
                                         transaction,
                                         optionalNewWallet,
                                         newCategoryType);
        }
        // Must be after updating the wallet balance because updating use old transaction data
        updateTransactionFields(transactionDto, transaction, optionalNewWallet);
        return transactionDao.save(transaction);
    }

    /**
     * Validates the transaction data.
     *
     * @param createTransactionDto DTO with transaction data
     * @param userId               logged in user ID
     * @param categoryType         type of the transaction category from the DTO
     * @param optionalWallet       optional wallet from the database with the ID from the DTO
     *
     * @throws BadFieldException     if category is not found
     * @throws BadRequestException   if wallet is not found
     * @throws ActionDeniedException if a user does not own the wallet
     */
    private static void validateCreateTransaction(CreateTransactionDto createTransactionDto,
                                                  long userId,
                                                  TransactionType categoryType,
                                                  Optional<Wallet> optionalWallet
    ) {
        // categoryType fetched from the database by category ID from the DTO
        if (categoryType == null) {
            throw new BadFieldException(
                    "Missing category with ID " + createTransactionDto.categoryId(),
                    ValidationCodes.CATEGORY_NOT_FOUND,
                    null,
                    "category");
        }
        if (optionalWallet.isEmpty()) {
            throw new BadRequestException(
                    "Wallet with ID " + createTransactionDto.walletId() + " does not exist.",
                    ValidationCodes.WALLET_NOT_FOUND,
                    null);
        }
        if (optionalWallet.get().getOwner().getId() != userId) {
            throw new ActionDeniedException(
                    "User with ID %d does not own wallet with ID %d"
                            .formatted(userId, optionalWallet.get().getId()));
        }
    }

    private static double roundTransactionAmount(double amount, CurrencyType currencyType) {
        return switch (currencyType) {
            // round to 2 decimal places
            case BASIC -> Math.floor(amount * 100.0) / 100.0;
            // round to 8 decimal places
            case CRYPTO -> Math.floor(amount * 100000000.0) / 100000000.0;
        };
    }

    /**
     * Calculates the wallet amount delta for new transaction state on update.
     *
     * @param oldType   transaction type for old transaction state
     * @param oldAmount transaction amount for old transaction state
     * @param newType   transaction type for new transaction state
     * @param newAmount transaction amount for new transaction state
     *
     * @return wallet amount delta for new transaction state
     */
    private static double calculateWalletNewAmountDelta(TransactionType oldType,
                                                        double oldAmount,
                                                        TransactionType newType,
                                                        double newAmount
    ) {
        double walletOldAmountDelta =
                oldType == TransactionType.INCOME ? oldAmount : -oldAmount;

        double walletNewAmountDelta =
                newType == TransactionType.INCOME ? newAmount : -newAmount;

        return BigDecimal.valueOf(walletNewAmountDelta)
                .subtract(BigDecimal.valueOf(walletOldAmountDelta))
                .doubleValue();
    }

    /**
     * Updates the transaction fields from the DTO.
     *
     * @param transactionDto    DTO with transaction data
     * @param transaction       transaction to update
     * @param optionalNewWallet new transaction state wallet. Must be present!
     */
    private static void updateTransactionFields(UpdateTransactionDto transactionDto,
                                                Transaction transaction,
                                                Optional<Wallet> optionalNewWallet
    ) {
        transaction.setDescription(
                transactionDto.description() == null
                        ? null
                        : transactionDto.description().trim().replaceAll("\\s+", " ")
        );
        transaction.setDate(new Date(transactionDto.date().getTime()));
        transaction.setAmount(roundTransactionAmount(
                transactionDto.amount(),
                transaction.getWallet().getCurrency().getType()
        ));
        transaction.setCategory(new TransactionCategory(transactionDto.categoryId()));
        transaction.setWallet(optionalNewWallet.get());
    }

    /**
     * Validates the transaction data for updating.
     *
     * @param updateTransactionDto DTO with transaction data
     * @param userId               logged in user ID
     * @param categoryType         type of the transaction category from the DTO
     * @param optionalWallet       optional wallet from the database with the ID from the DTO
     *
     * @throws BadFieldException     if category is not found
     * @throws BadRequestException   if a wallet is not found or transaction does not exist
     * @throws ActionDeniedException if a user does not own the transaction or wallet
     */
    private void validateUpdateTransaction(UpdateTransactionDto updateTransactionDto,
                                           long userId,
                                           TransactionType categoryType,
                                           Optional<Wallet> optionalWallet
    ) {
        if (!transactionDao.existsById(updateTransactionDto.id())) {
            throw new BadRequestException(
                    "Transaction with ID %d does not exist.".formatted(updateTransactionDto.id()),
                    ValidationCodes.TRANSACTION_NOT_FOUND,
                    null);
        }
        if (!transactionDao.isUserTransactionOwner(updateTransactionDto.id(), userId)) {
            throw new ActionDeniedException(
                    "User with ID %d is not the owner of transaction with ID %d"
                            .formatted(userId, updateTransactionDto.id()));
        }
        CreateTransactionDto createTransactionDto = new CreateTransactionDto(
                updateTransactionDto.description(),
                updateTransactionDto.date(),
                updateTransactionDto.amount(),
                updateTransactionDto.categoryId(),
                updateTransactionDto.walletId()
        );
        // Same validation as for creation transaction
        validateCreateTransaction(createTransactionDto, userId, categoryType, optionalWallet);
    }

    /**
     * If the wallet is not changed, updates the wallet balance. If the wallet is changed, updates
     * the wallet balance for the old and new wallets.
     *
     * @param transactionDto     DTO with transaction data
     * @param transaction        old transaction state
     * @param optionalNewWallet  new transaction state wallet. Must be present!
     * @param newTransactionType new transaction state category type
     */
    private void updateWalletBallanceOnUpdate(UpdateTransactionDto transactionDto,
                                              Transaction transaction,
                                              Optional<Wallet> optionalNewWallet,
                                              TransactionType newTransactionType
    ) {
        TransactionType oldTransactionType = transaction.getCategory().getType();
        double oldTransactionAmount = transaction.getAmount();

        double transactionAmount = roundTransactionAmount(
                transactionDto.amount(),
                transaction.getWallet().getCurrency().getType()
        );
        if (transaction.getWallet().getId() == transactionDto.walletId()) { // Wallet isn't changed
            double walletAmountDelta = calculateWalletNewAmountDelta(
                    oldTransactionType,
                    oldTransactionAmount,
                    newTransactionType,
                    transactionAmount
            );
            // Wallet is present because of validation
            walletService.updateWalletBalance(optionalNewWallet.get(), walletAmountDelta);
            return;
        }
        // Wallet changed
        Optional<Wallet> optionalOldWallet =
                walletService.getWalletForUpdate(transaction.getWallet().getId());

        // Wallet is present because of validation
        walletService.updateWalletBalance(optionalOldWallet.get(),
                                          oldTransactionType == TransactionType.INCOME
                                                  ? -oldTransactionAmount
                                                  : oldTransactionAmount);
        walletService.updateWalletBalance(optionalNewWallet.get(),
                                          newTransactionType == TransactionType.INCOME
                                                  ? transactionAmount
                                                  : -transactionAmount);
    }

}
