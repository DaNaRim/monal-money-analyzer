package com.danarim.monal.user.persistence.dao;

import com.danarim.monal.user.persistence.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenDao extends JpaRepository<Token, Long> {

    Token findByTokenValue(String tokenValue);
}
