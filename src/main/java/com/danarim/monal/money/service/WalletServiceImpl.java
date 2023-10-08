package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.ActionDeniedException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.InternalServerException;
import com.danarim.monal.money.persistence.dao.WalletDao;
import com.danarim.monal.money.persistence.model.Currency;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateWalletDto;
import com.danarim.monal.user.persistence.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for {@link Wallet} entities.
 */
@Service
public class WalletServiceImpl implements WalletService {

    private final WalletDao walletDao;

    public WalletServiceImpl(WalletDao walletDao) {
        this.walletDao = walletDao;
    }

    /**
     * Creates a new wallet for the user with the given id.
     *
     * @param walletDto    wallet data
     * @param loggedUserId id of the user that owns the wallet
     *
     * @return created wallet
     *
     * @throws BadRequestException if user with the given id does not exist
     * @throws BadFieldException   if currency is not valid
     */
    @Override
    public Wallet createWallet(CreateWalletDto walletDto, long loggedUserId) {
        if (walletDao.existsByOwnerIdAndName(loggedUserId, walletDto.name())) {
            throw new BadFieldException(
                    "Wallet with name %s already exists for user with id %d."
                            .formatted(walletDto.name(), loggedUserId),
                    "validation.wallet.name-for-user.alreadyExists",
                    null,
                    "name");
        }
        Currency parsedCurrency;
        try { // Check is valid currency
            parsedCurrency = Currency.valueOf(walletDto.currency().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadFieldException("Currency " + walletDto.currency() + " is not valid.", e,
                                        "validation.wallet.invalid.currency",
                                        null,
                                        "currency");
        }
        // Round balance based on a currency type
        double parsedBalance = switch (parsedCurrency.getType()) {
            // 2 decimal places
            case BASIC -> Math.floor(walletDto.balance() * 100.0) / 100.0;
            // 8 decimal places
            case CRYPTO -> Math.floor(walletDto.balance() * 100_000_000.0) / 100_000_000.0;
            default -> throw new InternalServerException(
                    // Should never happen
                    "Currency type %s is not supported.".formatted(parsedCurrency.getType())
            );
        };
        // User with only id is enough for linking in the database.
        return walletDao.save(new Wallet(walletDto.name().trim().replaceAll("\\s+", " "),
                                         parsedBalance,
                                         parsedCurrency,
                                         new User(loggedUserId)));
    }

    /**
     * Returns all wallets owned by the user with the given id. DON'T check if the user exists.
     *
     * @param loggedUserId id of the user that owns the wallets
     *
     * @return list of wallets owned by the user with the given id
     *
     * @throws BadRequestException if user with the given id does not exist
     */
    @Override
    public List<Wallet> getUserWallets(long loggedUserId) {
        return walletDao.findAllByOwnerId(loggedUserId);
    }

    /**
     * Locks the wallet with the given id for update. Require Transactional annotation.
     *
     * @param id id of the wallet
     *
     * @return wallet with the given id
     */
    @Override
    public Optional<Wallet> getWalletForUpdate(long id) {
        return walletDao.findById(id);
    }

    /**
     * Checks if the user with the given id owns the wallet with the given id.
     *
     * @param walletId id of the wallet
     * @param userId   id of the user
     *
     * @return true if the user owns the wallet, false otherwise. If the user or the wallet does not
     *         exist, returns false.
     */
    @Override
    public boolean isUserWalletOwner(long walletId, long userId) {
        return walletDao.isUserWalletOwner(walletId, userId);
    }

    /**
     * Returns currency of the wallet with the given id.
     *
     * @param walletId id of the wallet
     *
     * @return currency of the wallet with the given id
     */
    @Override
    public Currency getWalletCurrency(long walletId) {
        return walletDao.getWalletCurrency(walletId);
    }

    /**
     * For INTERNAL use only.
     *
     * <p>Use with {@link WalletService#getWalletForUpdate(long id)}.
     *
     * @param wallet wallet to update in the database.
     */
    @Override
    public void updateWallet(Wallet wallet) {
        walletDao.save(wallet);
    }

    @Override
    public Wallet updateWalletName(Long walletId, String newName, long loggedUserId) {
        if (!walletDao.existsById(walletId)) {
            throw new BadRequestException("Wallet with id " + walletId + " does not exist.",
                                          "validation.wallet.notFound",
                                          null);
        }
        if (!walletDao.isUserWalletOwner(walletId, loggedUserId)) {
            throw new ActionDeniedException("Wallet with id %d does not belong to user with id %d."
                                                    .formatted(walletId, loggedUserId));
        }
        Wallet wallet = walletDao.getById(walletId);
        wallet.setName(newName.trim().replaceAll("\\s+", " "));
        return walletDao.save(wallet);
    }

}
