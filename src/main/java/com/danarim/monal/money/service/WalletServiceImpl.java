package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.money.persistence.dao.WalletDao;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.web.dto.CreateWalletDto;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.User;
import org.springframework.stereotype.Service;

import java.util.Currency;

/**
 * Service for {@link Wallet} entities.
 */
@Service
public class WalletServiceImpl implements WalletService {

    private final WalletDao walletDao;
    private final UserDao userDao;

    public WalletServiceImpl(WalletDao walletDao,
                             UserDao userDao
    ) {
        this.walletDao = walletDao;
        this.userDao = userDao;
    }

    /**
     * Creates a new wallet for the user with the given id.
     *
     * @param walletDto wallet data
     * @param userId    id of the user that owns the wallet
     *
     * @return created wallet
     *
     * @throws BadRequestException if user with the given id does not exist
     * @throws BadFieldException   if currency is not valid
     */
    @Override
    public Wallet createWallet(CreateWalletDto walletDto, long userId) {
        if (!userDao.existsById(userId)) {
            throw new BadRequestException("User with id " + userId + " does not exist.",
                                          "validation.user.notFound",
                                          null);
        }

        try { // Check is valid currency
            Currency.getInstance(walletDto.currency());
        } catch (IllegalArgumentException e) {
            throw new BadFieldException("Currency " + walletDto.currency() + " is not valid.", e,
                                        "validation.wallet.invalid.currency",
                                        null,
                                        "currency");
        }
        // User with only id is enough for linking in the database.
        return walletDao.save(new Wallet(walletDto.name(),
                                         walletDto.balance(),
                                         walletDto.currency(),
                                         new User(userId)));
    }

}
