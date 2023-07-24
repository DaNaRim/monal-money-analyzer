package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.money.persistence.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link Wallet} entities.
 */
public interface WalletDao extends JpaRepository<Wallet, Long> {

    List<Wallet> findAllByOwnerId(long ownerId);

}
