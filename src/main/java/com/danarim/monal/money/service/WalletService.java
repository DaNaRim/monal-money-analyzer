package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateWalletDto;

/**
 * Service for {@link Wallet} entities.
 */
public interface WalletService {

    Wallet createWallet(CreateWalletDto walletDto, long userId);

}