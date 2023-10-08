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
     * For INTERNAL use only.
     *
     * <p>Use with {@link WalletService#getWalletForUpdate(long id)}.
     *
     * @param wallet wallet to update
     */
    void updateWallet(Wallet wallet);

    Wallet updateWalletName(Long walletId, String newName, long loggedUserId);

}
