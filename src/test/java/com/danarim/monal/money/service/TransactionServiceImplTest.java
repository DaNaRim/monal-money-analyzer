package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.ActionDeniedException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.money.persistence.dao.TransactionDao;
import com.danarim.monal.money.persistence.model.Currency;
import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.persistence.model.TransactionType;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateTransactionDto;
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
    void deleteTransaction() {
        when(transactionDao.existsById(1L))
                .thenReturn(true);
        when(transactionDao.isUserTransactionOwner(1L, 1L))
                .thenReturn(true);

        assertDoesNotThrow(() -> transactionService.deleteTransaction(1L, 1L));

        verify(transactionDao, times(1)).existsById(1L);
        verify(transactionDao, times(1)).isUserTransactionOwner(1L, 1L);
        verify(transactionDao, times(1)).deleteById(1L);
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

}
