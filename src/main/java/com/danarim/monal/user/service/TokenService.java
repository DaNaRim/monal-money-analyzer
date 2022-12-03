package com.danarim.monal.user.service;

import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.User;

public interface TokenService {

    Token createVerificationToken(User user);

    Token validateVerificationToken(String tokenValue);

    void deleteToken(Token token);
}