package com.danarim.monal.config.security.jwt;

import com.danarim.monal.user.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import javax.transaction.Transactional;

/**
 * Manages the {@link JwtTokenEntity} in the database.
 */
public interface JwtTokenDao extends JpaRepository<JwtTokenEntity, Long> {

    @Query("SELECT t.isBlocked FROM JwtTokenEntity t WHERE t.id = ?1")
    boolean isTokenBlocked(long id);

    @Query("UPDATE JwtTokenEntity t SET t.isBlocked = true WHERE t.id = ?1")
    @Modifying
    @Transactional
    void blockToken(long id);

    int countTokensByExpirationDateBefore(Date date);

    //Use Custom Query to optimize performance. Without it, spring executes a query for each token.
    @Query("DELETE FROM JwtTokenEntity t WHERE t.expirationDate <= ?1")
    @Modifying
    @Transactional
    void deleteByExpirationDateBefore(Date date);

    @Query("UPDATE JwtTokenEntity t SET t.isBlocked = true WHERE t.user = ?1")
    @Modifying
    @Transactional
    void blockAllTokensForUser(User user);

}
