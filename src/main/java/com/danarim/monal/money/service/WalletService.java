package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.model.Currency;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateWalletDto;

import java.util.List;
import java.util.Optional;

/**
 * Service for {@link Wallet} entities.
 */
public interface WalletService {

    Wallet createWallet(CreateWalletDto walletDto, long loggedUserId);

    List<Wallet> getUserWallets(long loggedUserId);

    Optional<Wallet> getWalletForUpdate(long id);

    boolean isUserWalletOwner(long walletId, long userId);

    Currency getWalletCurrency(long walletId);

    /**
     * Updates the balance of the given wallet by adding the given amount to the current balance.
     * {@link WalletService#getWalletForUpdate(long id)} must be used before calling this method.
     *
     * @param wallet     wallet to update
     * @param deltaAmount amount to add to the wallet balance (can be negative)
     */
    void updateWalletBalance(Wallet wallet, double deltaAmount);

    Wallet updateWalletName(Long walletId, String newName, long loggedUserId);

}
