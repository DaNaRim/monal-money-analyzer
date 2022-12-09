package com.danarim.monal.user.persistence.dao;

import com.danarim.monal.user.persistence.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.Date;

public interface TokenDao extends JpaRepository<Token, Long> {

    Token findByTokenValue(String tokenValue);

    int countTokensByExpiryDateBefore(Date date);

    //Use Custom Query to optimize performance. Without it, spring executes a query for each token.
    @Query("DELETE FROM Token t WHERE t.expiryDate <= ?1")
    @Modifying
    @Transactional
    void deleteByExpiryDateBefore(Date date);
}
