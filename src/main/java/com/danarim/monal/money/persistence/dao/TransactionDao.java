package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.money.persistence.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * DAO for {@link Transaction Transaction}.
 */
public interface TransactionDao extends JpaRepository<Transaction, Long> {

    List<Transaction> getTransactionsByWalletIdAndDateBetween(long walletId, Date from, Date to);

}
