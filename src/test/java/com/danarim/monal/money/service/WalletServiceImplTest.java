package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.ActionDeniedException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.money.persistence.dao.WalletDao;
import com.danarim.monal.money.persistence.model.Currency;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateWalletDto;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    private final WalletDao walletDao = mock(WalletDao.class);

    @InjectMocks
    private WalletServiceImpl walletService;

    // Create wallet

    @Test
    void createWallet_basicCurrency() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 1.0112, "USD");

        when(walletDao.existsByOwnerIdAndName(1L, "test")).thenReturn(false);
        when(walletDao.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wallet result = walletService.createWallet(walletDto, 1L);

        assertEquals(1.01, result.getBalance());
        verify(walletDao).existsByOwnerIdAndName(1L, "test");
        verify(walletDao).save(any(Wallet.class));
    }

    @Test
    void createWallet_cryptoCurrency() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 1.0112, "BTC");

        when(walletDao.existsByOwnerIdAndName(1L, "test")).thenReturn(false);
        when(walletDao.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wallet result = walletService.createWallet(walletDto, 1L);

        assertEquals(1.0112, result.getBalance());

        verify(walletDao).existsByOwnerIdAndName(1L, "test");
        verify(walletDao).save(any(Wallet.class));
    }

    @Test
    void createWallet_userHasWalletWithName_BadFieldException() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 0.0, "USD");

        when(walletDao.existsByOwnerIdAndName(1L, "test")).thenReturn(true);

        BadFieldException e = assertThrows(BadFieldException.class,
                                           () -> walletService.createWallet(walletDto, 1L));

        assertEquals("validation.wallet.name-for-user.alreadyExists", e.getMessageCode());

        verify(walletDao).existsByOwnerIdAndName(1L, "test");
        verify(walletDao, never()).save(any(Wallet.class));
    }

    @Test
    void createWallet_invalidCurrency_throwException() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 0.0, "invalid");

        when(walletDao.existsByOwnerIdAndName(1L, "test")).thenReturn(false);

        BadRequestException e = assertThrows(BadRequestException.class,
                                             () -> walletService.createWallet(walletDto, 1L));

        assertEquals("validation.wallet.currency.invalid", e.getMessageCode());
        verify(walletDao).existsByOwnerIdAndName(1L, "test");
        verify(walletDao, never()).save(any(Wallet.class));
    }

    // Update wallet balance

    @Test
    void updateWalletBalance_cryptoCurrency() {
        Wallet wallet = new Wallet("test", 0.00000001, Currency.BTC, new User(1L));

        walletService.updateWalletBalance(wallet, 0.00000007);

        assertEquals(0.00000008, wallet.getBalance());

        verify(walletDao, times(1)).save(wallet);
    }

    // Update wallet name

    @Test
    void updateWalletName() {
        Wallet wallet = new Wallet("test", 0.0, Currency.USD, new User(1L));

        when(walletDao.existsById(1L)).thenReturn(true);
        when(walletDao.isUserWalletOwner(1L, 1L)).thenReturn(true);
        when(walletDao.getById(1L)).thenReturn(wallet);
        when(walletDao.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        walletService.updateWalletName(1L, " test   2", 1L);

        assertEquals("test 2", wallet.getName()); // Unnecessary space is removed

        verify(walletDao, times(1)).existsById(1L);
        verify(walletDao, times(1)).isUserWalletOwner(1L, 1L);
        verify(walletDao, times(1)).getById(1L);
        verify(walletDao, times(1)).save(any(Wallet.class));
    }

    @Test
    void updateWalletName_walletNotFound_BadRequestException() {
        when(walletDao.existsById(1L)).thenReturn(false);

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> walletService.updateWalletName(1L, "test", 1L));

        assertEquals("validation.wallet.notFound", e.getMessageCode());

        verify(walletDao).existsById(1L);
        verify(walletDao, never()).isUserWalletOwner(1L, 1L);
        verify(walletDao, never()).getById(1L);
        verify(walletDao, never()).save(any(Wallet.class));
    }

    @Test
    void updateWalletName_userNotWalletOwner_ActionDeniedException() {
        when(walletDao.existsById(1L)).thenReturn(true);
        when(walletDao.isUserWalletOwner(1L, 1L)).thenReturn(false);

        assertThrows(ActionDeniedException.class,
                     () -> walletService.updateWalletName(1L, "test", 1L));

        verify(walletDao).existsById(1L);
        verify(walletDao).isUserWalletOwner(1L, 1L);
        verify(walletDao, never()).getById(1L);
        verify(walletDao, never()).save(any(Wallet.class));
    }

    // Delete wallet

    @Test
    void deleteWallet() {
        when(walletDao.existsById(1L)).thenReturn(true);
        when(walletDao.isUserWalletOwner(1L, 1L)).thenReturn(true);
        when(walletDao.countWalletTransactions(1L)).thenReturn(0L);

        walletService.deleteWallet(1L, 1L);

        verify(walletDao, times(1)).existsById(1L);
        verify(walletDao, times(1)).isUserWalletOwner(1L, 1L);
        verify(walletDao, times(1)).countWalletTransactions(1L);
        verify(walletDao, times(1)).deleteById(1L);
    }

    @Test
    void deleteWallet_notFound_BadRequestException() {
        when(walletDao.existsById(1L)).thenReturn(false);

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> walletService.deleteWallet(1L, 1L));

        assertEquals("validation.wallet.notFound", e.getMessageCode());

        verify(walletDao).existsById(1L);
        verify(walletDao, never()).isUserWalletOwner(1L, 1L);
        verify(walletDao, never()).countWalletTransactions(1L);
        verify(walletDao, never()).deleteById(1L);
    }

    @Test
    void deleteWallet_userNotWalletOwner_ActionDeniedException() {
        when(walletDao.existsById(1L)).thenReturn(true);
        when(walletDao.isUserWalletOwner(1L, 1L)).thenReturn(false);

        assertThrows(ActionDeniedException.class,
                     () -> walletService.deleteWallet(1L, 1L));

        verify(walletDao).existsById(1L);
        verify(walletDao).isUserWalletOwner(1L, 1L);
        verify(walletDao, never()).countWalletTransactions(1L);
        verify(walletDao, never()).deleteById(1L);
    }

    @Test
    void deleteWallet_hasTransaction_BadRequestException() {
        when(walletDao.existsById(1L)).thenReturn(true);
        when(walletDao.isUserWalletOwner(1L, 1L)).thenReturn(true);
        when(walletDao.countWalletTransactions(1L)).thenReturn(1L);

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> walletService.deleteWallet(1L, 1L));

        assertEquals("validation.wallet.delete.hasTransactions", e.getMessageCode());

        verify(walletDao).existsById(1L);
        verify(walletDao).isUserWalletOwner(1L, 1L);
        verify(walletDao).countWalletTransactions(1L);
        verify(walletDao, never()).deleteById(1L);
    }

    // Count wallet transactions


    @Test
    void countWalletTransactions() {
        when(walletDao.existsById(1L)).thenReturn(true);
        when(walletDao.isUserWalletOwner(1L, 1L)).thenReturn(true);
        when(walletDao.countWalletTransactions(1L)).thenReturn(34L);

        long result = walletService.countWalletTransactions(1L, 1L);

        assertEquals(34L, result);

        verify(walletDao).existsById(1L);
        verify(walletDao).isUserWalletOwner(1L, 1L);
        verify(walletDao).countWalletTransactions(1L);
    }

    @Test
    void countWalletTransactions_walletNotFound_BadRequestException() {
        when(walletDao.existsById(1L)).thenReturn(false);

        BadRequestException e = assertThrows(
                BadRequestException.class,
                () -> walletService.countWalletTransactions(1L, 1L));

        assertEquals("validation.wallet.notFound", e.getMessageCode());

        verify(walletDao).existsById(1L);
        verify(walletDao, never()).isUserWalletOwner(1L, 1L);
        verify(walletDao, never()).countWalletTransactions(1L);
    }

    @Test
    void countWalletTransactions_userNotWalletOwner_ActionDeniedException() {
        when(walletDao.existsById(1L)).thenReturn(true);
        when(walletDao.isUserWalletOwner(1L, 1L)).thenReturn(false);

        assertThrows(ActionDeniedException.class,
                     () -> walletService.countWalletTransactions(1L, 1L));

        verify(walletDao).existsById(1L);
        verify(walletDao).isUserWalletOwner(1L, 1L);
        verify(walletDao, never()).countWalletTransactions(1L);
    }

}
