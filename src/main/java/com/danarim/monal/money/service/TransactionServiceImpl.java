package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.ActionDeniedException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.InternalServerException;
import com.danarim.monal.money.persistence.dao.TransactionDao;
import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.TransactionType;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateTransactionDto;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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

        validateTransaction(createTransactionDto, userId, categoryType, optionalWallet);

        Wallet wallet = optionalWallet.get(); // Wallet is present because of validation

        Transaction result = transactionDao.save(new Transaction(
                createTransactionDto.description(),
                new Date(createTransactionDto.date().getTime()),
                createTransactionDto.amount(),
                new TransactionCategory(createTransactionDto.categoryId()),
                wallet
        ));
        updateWalletBalance(wallet, createTransactionDto.amount(), categoryType);

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
                                          "validation.transaction.date-from-after-date-to",
                                          null);
        }
        return transactionDao.getTransactionsByWalletIdAndDateBetween(walletId, from, to);
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
    private static void validateTransaction(CreateTransactionDto createTransactionDto,
                                            long userId,
                                            TransactionType categoryType,
                                            Optional<Wallet> optionalWallet
    ) {
        if (categoryType == null) {
            throw new BadFieldException(
                    "Missing category with ID " + createTransactionDto.categoryId(),
                    "validation.category.notFound",
                    null,
                    "category");
        }
        if (optionalWallet.isEmpty()) {
            throw new BadRequestException(
                    "Wallet with ID " + createTransactionDto.walletId() + " does not exist.",
                    "validation.wallet.notFound",
                    null);
        }
        if (optionalWallet.get().getOwner().getId() != userId) {
            throw new ActionDeniedException(
                    "User with ID %d does not own wallet with ID %d"
                            .formatted(userId, optionalWallet.get().getId()));
        }
    }

    /**
     * Updates the balance of the wallet after a transaction.
     *
     * @param wallet       wallet to update
     * @param amount       amount of the transaction
     * @param categoryType type of the transaction category
     *
     * @throws InternalServerException if the transaction type is not supported for updating the
     *                                 wallet balance (should never happen)
     */
    private void updateWalletBalance(Wallet wallet, double amount, TransactionType categoryType) {
        switch (categoryType) {
            case INCOME -> wallet.setBalance(wallet.getBalance() + amount);
            case OUTCOME -> wallet.setBalance(wallet.getBalance() - amount);
            default -> throw new InternalServerException(
                    "Unsupported transaction type " + categoryType + " for updating wallet balance."
            );
        }
        walletService.updateWallet(wallet);
    }

}
