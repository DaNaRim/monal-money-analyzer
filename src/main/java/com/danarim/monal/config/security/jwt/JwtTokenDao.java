package com.danarim.monal.config.security.jwt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;

public interface JwtTokenDao extends JpaRepository<JwtTokenEntity, Long> {

    @Query("SELECT t.isBlocked FROM JwtTokenEntity t WHERE t.id = ?1")
    boolean isTokenBlocked(long id);

    @Query("UPDATE JwtTokenEntity t SET t.isBlocked = true WHERE t.id = ?1")
    @Modifying
    @Transactional
    void blockToken(long id);
}
