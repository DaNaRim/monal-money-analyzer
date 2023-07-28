package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * DAO for {@link TransactionCategory}.
 */
public interface TransactionCategoryDao extends JpaRepository<TransactionCategory, Long> {

    @Query("SELECT tc.type FROM TransactionCategory tc WHERE tc.id = ?1")
    TransactionType getTypeById(long id);

}
