package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.ActionDeniedException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.money.persistence.dao.WalletDao;
import com.danarim.monal.money.persistence.model.Currency;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateWalletDto;
import com.danarim.monal.user.persistence.dao.UserDao;
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
    private final UserDao userDao = mock(UserDao.class);

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void createWallet_basicCurrency() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 1.0112, "USD");

        when(userDao.existsById(1L)).thenReturn(true);
        when(walletDao.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wallet result = walletService.createWallet(walletDto, 1L);

        assertEquals(1.01, result.getBalance());
        verify(userDao).existsById(1L);
        verify(walletDao).save(any(Wallet.class));
    }

    @Test
    void createWallet_cryptoCurrency() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 1.0112, "BTC");

        when(userDao.existsById(1L)).thenReturn(true);
        when(walletDao.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Wallet result = walletService.createWallet(walletDto, 1L);

        assertEquals(1.0112, result.getBalance());
        verify(userDao).existsById(1L);
        verify(walletDao).save(any(Wallet.class));
    }

    @Test
    void createWallet_userNotFound_BadRequestException() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 0.0, "USD");

        when(userDao.existsById(1L)).thenReturn(false);

        BadRequestException e = assertThrows(BadRequestException.class,
                                             () -> walletService.createWallet(walletDto, 1L));

        assertEquals("validation.user.notFound", e.getMessageCode());
        verify(userDao).existsById(1L);
        verify(walletDao, never()).save(any(Wallet.class));
    }

    @Test
    void createWallet_userHasWalletWithName_BadFieldException() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 0.0, "USD");

        when(userDao.existsById(1L)).thenReturn(true);
        when(walletDao.existsByOwnerIdAndName(1L, "test")).thenReturn(true);

        BadFieldException e = assertThrows(BadFieldException.class,
                                           () -> walletService.createWallet(walletDto, 1L));

        assertEquals("validation.wallet.name-for-user.alreadyExists", e.getMessageCode());
        verify(userDao).existsById(1L);
        verify(walletDao).existsByOwnerIdAndName(1L, "test");
        verify(walletDao, never()).save(any(Wallet.class));
    }

    @Test
    void createWallet_invalidCurrency_throwException() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 0.0, "invalid");

        when(userDao.existsById(1L)).thenReturn(true);

        BadRequestException e = assertThrows(BadRequestException.class,
                                             () -> walletService.createWallet(walletDto, 1L));

        assertEquals("validation.wallet.invalid.currency", e.getMessageCode());
        verify(userDao).existsById(1L);
        verify(walletDao, never()).save(any(Wallet.class));
    }

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

}
