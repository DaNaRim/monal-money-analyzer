package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.money.persistence.model.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * DAO for {@link TransactionCategory}.
 */
public interface TransactionCategoryDao extends JpaRepository<TransactionCategory, Long> {

}
