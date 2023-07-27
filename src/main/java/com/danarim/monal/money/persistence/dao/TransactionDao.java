package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.money.persistence.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO for {@link Transaction Transaction}.
 */
public interface TransactionDao extends JpaRepository<Transaction, Long> {

}
