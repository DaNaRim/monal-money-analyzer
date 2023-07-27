package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.money.persistence.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

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

}
