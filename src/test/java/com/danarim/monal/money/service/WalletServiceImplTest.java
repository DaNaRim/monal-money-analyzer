package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.money.persistence.dao.WalletDao;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateWalletDto;
import com.danarim.monal.user.persistence.dao.UserDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    private final WalletDao walletDao = mock(WalletDao.class);
    private final UserDao userDao = mock(UserDao.class);

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void createWallet() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 0.0, "USD");

        when(userDao.existsById(1L)).thenReturn(true);
        when(walletDao.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        walletService.createWallet(walletDto, 1L);

        verify(userDao).existsById(1L);
        verify(walletDao).save(any(Wallet.class));
    }

    @Test
    void createWallet_userNotFound_throwException() {
        CreateWalletDto walletDto = new CreateWalletDto("test", 0.0, "USD");

        when(userDao.existsById(1L)).thenReturn(false);

        BadRequestException e = assertThrows(BadRequestException.class,
                                             () -> walletService.createWallet(walletDto, 1L));

        assertEquals("validation.user.notFound", e.getMessageCode());
        verify(userDao).existsById(1L);
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

}
