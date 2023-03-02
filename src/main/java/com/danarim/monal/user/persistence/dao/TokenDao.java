package com.danarim.monal.user.persistence.dao;

import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.TokenType;
import com.danarim.monal.user.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import javax.transaction.Transactional;

/**
 * Manages the {@link Token} in the database.
 */
public interface TokenDao extends JpaRepository<Token, Long> {

    Token findByTokenValue(String tokenValue);

    @Query("select max(t.createdDate) from Token t where t.user = ?1 and t.tokenType = ?2")
    Date findLastTokenCreationDate(User user, TokenType type);

    int countTokensByExpirationDateBefore(Date date);

    //Use Custom Query to optimize performance. Without it, spring executes a query for each token.
    @Query("DELETE FROM Token t WHERE t.expirationDate <= ?1")
    @Modifying
    @Transactional
    void deleteByExpirationDateBefore(Date date);

}
