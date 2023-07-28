package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.money.persistence.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import javax.persistence.LockModeType;

/**
 * Repository for {@link Wallet} entities.
 */
public interface WalletDao extends JpaRepository<Wallet, Long> {

    List<Wallet> findAllByOwnerId(long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findById(long id);

    @Query(
            "SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END AS is_user_wallet_owner"
                    + " FROM Wallet w"
                    + " WHERE w.id = :walletId"
                    + " AND w.owner.id = :userId"
    )
    boolean isUserWalletOwner(long walletId, long userId);

}
