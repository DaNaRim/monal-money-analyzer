package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.ActionDeniedException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.money.persistence.dao.TransactionDao;
import com.danarim.monal.money.persistence.model.Currency;
import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.TransactionType;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateTransactionDto;
import com.danarim.monal.money.web.dto.UpdateTransactionDto;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    private final TransactionCategoryService categoryService =
            mock(TransactionCategoryService.class);
    private final WalletService walletService = mock(WalletService.class);
    private final TransactionDao transactionDao = mock(TransactionDao.class);

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        when(transactionDao.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createTransaction_incomeCategory() {
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                "test", new Date(), 1.0, 1L, 1L
        );
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));

        when(categoryService.getCategoryType(transactionDto.categoryId()))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(transactionDto.walletId()))
                .thenReturn(Optional.of(wallet));

        transactionService.createTransaction(transactionDto, 1L);

        assertEquals(1.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(transactionDto.categoryId());
        verify(walletService, times(1)).getWalletForUpdate(transactionDto.walletId());
        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void createTransaction_outcomeCategory() {
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                null, new Date(), 1.0, 1L, 1L
        );
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));

        when(categoryService.getCategoryType(transactionDto.categoryId()))
                .thenReturn(TransactionType.OUTCOME);
        when(walletService.getWalletForUpdate(transactionDto.walletId()))
                .thenReturn(Optional.of(wallet));

        transactionService.createTransaction(transactionDto, 1L);

        assertEquals(-1.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(transactionDto.categoryId());
        verify(walletService, times(1)).getWalletForUpdate(transactionDto.walletId());
        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void createTransaction_basicCurrency_roundAmountToTwoDigitsAfterComma() {
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                "test", new Date(), 1.1234, 1L, 1L
        );
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));

        when(categoryService.getCategoryType(transactionDto.categoryId()))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(transactionDto.walletId()))
                .thenReturn(Optional.of(wallet));

        Transaction result = transactionService.createTransaction(transactionDto, 1L);

        assertEquals(1.12, wallet.getBalance());
        assertEquals(1.12, result.getAmount());

        verify(categoryService, times(1)).getCategoryType(transactionDto.categoryId());
        verify(walletService, times(1)).getWalletForUpdate(transactionDto.walletId());
        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void createTransaction_cryptoCurrency_roundAmountToTwoEightDigitsAfterComma() {
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                "test", new Date(), 1.1234567890, 1L, 1L
        );
        Wallet wallet = new Wallet("test", 0.0, Currency.BTC, new User(1L));

        when(categoryService.getCategoryType(transactionDto.categoryId()))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(transactionDto.walletId()))
                .thenReturn(Optional.of(wallet));

        Transaction result = transactionService.createTransaction(transactionDto, 1L);

        assertEquals(1.12345678, wallet.getBalance());
        assertEquals(1.12345678, result.getAmount());

        verify(categoryService, times(1)).getCategoryType(transactionDto.categoryId());
        verify(walletService, times(1)).getWalletForUpdate(transactionDto.walletId());
        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void createTransaction_invalidCategory_BadFieldException() {
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                "test", new Date(), 1.0, 1L, 1L
        );
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));

        when(categoryService.getCategoryType(transactionDto.categoryId()))
                .thenReturn(null);
        when(walletService.getWalletForUpdate(transactionDto.walletId()))
                .thenReturn(Optional.of(wallet));

        BadFieldException e = assertThrows(
                BadFieldException.class,
                () -> transactionService.createTransaction(transactionDto, 1L));

        assertEquals("category", e.getField());
        assertEquals("validation.category.notFound", e.getMessageCode());
        assertEquals(0.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(transactionDto.categoryId());
        verify(walletService, times(1)).getWalletForUpdate(transactionDto.walletId());
        verify(transactionDao, never()).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(wallet);
    }

    @Test
    void createTransaction_invalidWallet_BadRequestException() {
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                "test", new Date(), 1.0, 1L, 1L
        );
        when(categoryService.getCategoryType(transactionDto.categoryId()))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(transactionDto.walletId()))
                .thenReturn(Optional.empty());

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> transactionService.createTransaction(transactionDto, 1L));

        assertEquals("validation.wallet.notFound", e.getMessageCode());

        verify(categoryService, times(1)).getCategoryType(transactionDto.categoryId());
        verify(walletService, times(1)).getWalletForUpdate(transactionDto.walletId());
        verify(transactionDao, never()).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void createTransaction_walletNotBelongsToUser_ActionDeniedException() {
        CreateTransactionDto transactionDto = new CreateTransactionDto(
                "test", new Date(), 1.0, 1L, 1L
        );
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(2L));

        when(categoryService.getCategoryType(transactionDto.categoryId()))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(transactionDto.walletId()))
                .thenReturn(Optional.of(wallet));

        ActionDeniedException e = assertThrows(
                ActionDeniedException.class,
                () -> transactionService.createTransaction(transactionDto, 1L));

        assertNotNull(e.getMessage());

        verify(categoryService, times(1)).getCategoryType(transactionDto.categoryId());
        verify(walletService, times(1)).getWalletForUpdate(transactionDto.walletId());
        verify(transactionDao, never()).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void getTransactionsBetweenDates() {
        when(walletService.isUserWalletOwner(anyLong(), anyLong()))
                .thenReturn(true);

        assertDoesNotThrow(() -> transactionService.getTransactionsBetweenDates(
                new Date(1L), new Date(), 1L, 1L));
    }

    @Test
    void getTransactionsBetweenDates_UserNotWalletOwner_AccessDeniedException() {
        when(walletService.isUserWalletOwner(anyLong(), anyLong()))
                .thenReturn(false);

        Date from = new Date(1L);
        Date to = new Date();

        AccessDeniedException e = assertThrows(
                AccessDeniedException.class,
                () -> transactionService.getTransactionsBetweenDates(from, to, 1L, 1L));

        assertNotNull(e.getMessage());
    }

    @Test
    void getTransactionsBetweenDates_DateFromAfterDateTo_BadRequestException() {
        when(walletService.isUserWalletOwner(anyLong(), anyLong()))
                .thenReturn(true);

        Date from = new Date();
        Date to = new Date(1L);

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> transactionService.getTransactionsBetweenDates(from, to, 1L, 1L));

        assertNotNull(e.getMessage());
        assertEquals("validation.transaction.date-from-after-date-to", e.getMessageCode());
    }

    @Test
    void deleteTransaction_IncomeCategory() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        Transaction transaction = new Transaction(
                "test", new Date(), 1.0, category, wallet
        );
        when(transactionDao.existsById(1L)).thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(transactionDao.getById(1L)).thenReturn(transaction);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L, 1L));

        assertEquals(-1.0, wallet.getBalance());

        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(transactionDao, times(1)).getById(1L);
        verify(transactionDao, times(1)).deleteById(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
    }

    @Test
    void deleteTransaction_OutcomeCategory() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        Transaction transaction = new Transaction(
                "test", new Date(), 1.0, category, wallet
        );
        when(transactionDao.existsById(1L)).thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(transactionDao.getById(1L)).thenReturn(transaction);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L, 1L));

        assertEquals(-1.0, wallet.getBalance());

        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(transactionDao, times(1)).getById(1L);
        verify(transactionDao, times(1)).deleteById(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
    }

    @Test
    void deleteTransaction_transactionNotFound_BadRequestException() {
        when(transactionDao.existsById(1L))
                .thenReturn(false);

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> transactionService.deleteTransaction(1L, 1L));

        assertEquals("validation.transaction.notFound", e.getMessageCode());

        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, never()).isUserTransactionOwner(1L, 1L);
        verify(transactionDao, never()).deleteById(1L);
    }

    @Test
    void deleteTransaction_userNotTransactionOwner_ActionDeniedException() {
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(false);

        assertThrows(ActionDeniedException.class,
                     () -> transactionService.deleteTransaction(1L, 1L));

        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(transactionDao, never()).deleteById(1L);
    }

    @Test
    void updateTransaction_updateDescription_removeUnnecessarySpaces() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "old", transactionDate, 1.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "ne  w  ", transactionDate, 1.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("ne w", result.getDescription());
        assertEquals(1.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void updateTransaction_updateDescriptionToNull() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "old", transactionDate, 1.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, null, transactionDate, 1.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertNull(result.getDescription());
        assertEquals(1.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void updateTransaction_updateDate() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Transaction oldState = new Transaction(
                "test", new Date(), 1.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        Date newDate = new Date();
        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", newDate, 1.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(1.0, result.getAmount());
        assertEquals(newDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void updateTransaction_incomeTrUpdateAmountUp() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(2.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());
        // Old: Income 1.0, new: Income 2.0. Old balance: 0.0, new balance: 2.0 - 1.0 = 1.0
        assertEquals(1.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void updateTransaction_incomeTrUpdateAmountDown() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 2.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 1.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(1.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());
        // Old: Income 2.0, new: Income 1.0. Old balance: 0.0, new balance: 1.0 - 2.0 = -1.0
        assertEquals(-1.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void updateTransaction_outcomeTrUpdateAmountUp() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.OUTCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.OUTCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(2.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());
        // Old: Outcome 1.0, new: Outcome 2.0. Old balance: 0.0, new balance: -2.0 - -1.0 = -1.0
        assertEquals(-1.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void updateTransaction_outcomeTrUpdateAmountDown() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.OUTCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 2.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.OUTCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 1.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(1.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());
        // Old: Outcome 2.0, new: Outcome 1.0. Old balance: 0.0, new balance: -1.0 - -2.0 = 1.0
        assertEquals(1.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void updateTransaction_outcomeTrUpdateAmountRoundBasicPrecision() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.OUTCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 2.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.OUTCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 1.12345, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(1.12, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());
        // Old: Outcome 2.0, new: Outcome 1.12. Old balance: 0.0, new balance: -1.12 - -2.0 = 0.88
        assertEquals(0.88, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void updateTransaction_outcomeTrUpdateAmountRoundCryptoPrecision() {
        Wallet wallet = new Wallet("test", 0.0, Currency.BTC, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.OUTCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 2.12345678, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.OUTCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.BTC);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 1.1234567788, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(1.12345677, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());
        // Old: Outcome 2.12345678, new: Outcome 1.12345677.
        // Old balance: 0.0, new balance: -1.12345677 - -2.12345678 = 1.00000001
        assertEquals(1.00000001, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void updateTransaction_updateCategorySameType() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 2.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(2.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());
        assertEquals(0.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void updateTransaction_updateCategoryIncomeToOutcome() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 2.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.OUTCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 1.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(1.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());
        // Old: Income 2.0, new: Outcome 1.0. Old balance: 0.0, new balance: -1.0 - 2.0 = -3.0
        assertEquals(-3.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void updateTransaction_updateCategoryOutcomeToIncome() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.OUTCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 2.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 1.0, 1L, 1L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(1.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(wallet, result.getWallet());
        // Old: Outcome 2.0, new: Income 1.0. Old balance: 0.0, new balance: 1.0 - -2.0 = 3.0
        assertEquals(3.0, wallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(1L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(wallet);
    }

    @Test
    void updateTransaction_updateWallet() {
        Wallet oldWallet = new Wallet("test1", 0.0, Currency.USD, new User(1L));
        oldWallet.setId(1L);
        Wallet newWallet = new Wallet("test2", 0.0, Currency.USD, new User(1L));
        newWallet.setId(2L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.OUTCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, oldWallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.OUTCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(oldWallet));
        when(walletService.getWalletForUpdate(2L))
                .thenReturn(Optional.of(newWallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(2L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 1.0, 1L, 2L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(1.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(newWallet, result.getWallet());
        // Old: Outcome 1.0, Old balance: 0.0, new balance: 0.0 - -1.0 = 1.0
        assertEquals(1.0, oldWallet.getBalance());
        // New: Outcome 1.0, Old balance: 0.0, new balance: 0.0 + -1.0 = -1.0
        assertEquals(-1.0, newWallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(walletService, times(1)).getWalletForUpdate(2L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(2L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(oldWallet);
        verify(walletService, times(1)).updateWallet(newWallet);
    }

    @Test
    void updateTransaction_updateWalletAndCategoryTypeOutcomeToIncomeAndAmount() {
        Wallet oldWallet = new Wallet("test1", 0.0, Currency.USD, new User(1L));
        oldWallet.setId(1L);
        Wallet newWallet = new Wallet("test2", 0.0, Currency.USD, new User(1L));
        newWallet.setId(2L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.OUTCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, oldWallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(oldWallet));
        when(walletService.getWalletForUpdate(2L))
                .thenReturn(Optional.of(newWallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(2L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 2L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(2.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(newWallet, result.getWallet());
        // Old: Outcome 1.0, Old balance: 0.0, new balance: 0.0 - -1.0 = 1.0
        assertEquals(1.0, oldWallet.getBalance());
        // New: Income 2.0, Old balance: 0.0, new balance: 0.0 + 2.0 = 1.0
        assertEquals(2.0, newWallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(walletService, times(1)).getWalletForUpdate(2L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(2L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(oldWallet);
        verify(walletService, times(1)).updateWallet(newWallet);
    }

    @Test
    void updateTransaction_updateWalletAndCategoryTypeIncomeToOutcomeAndAmount() {
        Wallet oldWallet = new Wallet("test1", 0.0, Currency.USD, new User(1L));
        oldWallet.setId(1L);
        Wallet newWallet = new Wallet("test2", 0.0, Currency.USD, new User(1L));
        newWallet.setId(2L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, oldWallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.OUTCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(oldWallet));
        when(walletService.getWalletForUpdate(2L))
                .thenReturn(Optional.of(newWallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(2L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 2L
        );
        Transaction result = transactionService.updateTransaction(transactionDto, 1L);

        assertEquals("test", result.getDescription());
        assertEquals(2.0, result.getAmount());
        assertEquals(transactionDate, result.getDate());
        assertEquals(category.getId(), result.getCategory().getId());
        assertEquals(newWallet, result.getWallet());
        // Old: Income 1.0, Old balance: 0.0, new balance: 0.0 - 1.0 = -1.0
        assertEquals(-1.0, oldWallet.getBalance());
        // New: Outcome 2.0, Old balance: 0.0, new balance: 0.0 + -2.0 = -2.0
        assertEquals(-2.0, newWallet.getBalance());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(walletService, times(1)).getWalletForUpdate(2L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).findById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(walletService, times(1)).getWalletCurrency(2L);

        verify(transactionDao, times(1)).save(any(Transaction.class));
        verify(walletService, times(1)).updateWallet(oldWallet);
        verify(walletService, times(1)).updateWallet(newWallet);
    }

    @Test
    void updateTransaction_notFound_BadRequestException() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(false);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 1L
        );
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            transactionService.updateTransaction(transactionDto, 1L);
        });
        assertEquals("validation.transaction.notFound", exception.getMessageCode());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);

        verify(transactionDao, never()).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void updateTransaction_UserNotTransactionOwner_ActionDeniedException() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(false);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 1L
        );
        assertThrows(ActionDeniedException.class, () -> {
            transactionService.updateTransaction(transactionDto, 1L);
        });

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);

        verify(transactionDao, never()).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void updateTransaction_CategoryNotFound_BadFieldException() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(null);
        when(walletService.getWalletForUpdate(1L))
                .thenReturn(Optional.of(wallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 1L
        );
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            transactionService.updateTransaction(transactionDto, 1L);
        });
        assertEquals("validation.category.notFound", exception.getMessageCode());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(1L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);

        verify(transactionDao, never()).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void updateTransaction_NewWalletNotFound_BadFieldException() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        wallet.setId(1L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, wallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(2L))
                .thenReturn(Optional.empty());
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 2L
        );
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            transactionService.updateTransaction(transactionDto, 1L);
        });
        assertEquals("validation.wallet.notFound", exception.getMessageCode());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(2L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);

        verify(transactionDao, never()).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void updateTransaction_UserNotNewWalletOwner_ActionDeniedException() {
        Wallet oldWallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        oldWallet.setId(1L);
        Wallet newWallet = new Wallet("test", 0.0, Currency.USD, new User(2L));
        newWallet.setId(2L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, oldWallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(2L))
                .thenReturn(Optional.of(newWallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.USD);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 2L
        );
        assertThrows(ActionDeniedException.class, () -> {
            transactionService.updateTransaction(transactionDto, 1L);
        });

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(2L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);

        verify(transactionDao, never()).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

    @Test
    void updateTransaction_NewWalletHasDifferentCurrency_BadRequestException() {
        Wallet oldWallet = new Wallet("test", 0.0, Currency.USD, new User(1L));
        oldWallet.setId(1L);
        Wallet newWallet = new Wallet("test", 0.0, Currency.UAH, new User(1L));
        newWallet.setId(2L);
        TransactionCategory category = new TransactionCategory(
                "test", TransactionType.INCOME, null
        );
        category.setId(1L);
        Date transactionDate = new Date();
        Transaction oldState = new Transaction(
                "test", transactionDate, 1.0, category, oldWallet
        );
        oldState.setId(1L);

        when(categoryService.getCategoryType(1L))
                .thenReturn(TransactionType.INCOME);
        when(walletService.getWalletForUpdate(2L))
                .thenReturn(Optional.of(newWallet));
        when(transactionDao.findById(1L))
                .thenReturn(Optional.of(oldState));
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);
        when(walletService.getWalletCurrency(1L))
                .thenReturn(Currency.UAH);

        UpdateTransactionDto transactionDto = new UpdateTransactionDto(
                1L, "test", transactionDate, 2.0, 1L, 2L
        );
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            transactionService.updateTransaction(transactionDto, 1L);
        });
        assertEquals("validation.transaction.wallet-has-different-currency",
                     exception.getMessageCode());

        verify(categoryService, times(1)).getCategoryType(1L);
        verify(walletService, times(1)).getWalletForUpdate(2L);
        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);

        verify(transactionDao, never()).save(any(Transaction.class));
        verify(walletService, never()).updateWallet(any(Wallet.class));
    }

}
