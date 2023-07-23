package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.money.persistence.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Wallet} entities.
 */
public interface WalletDao extends JpaRepository<Wallet, Long> {

}
