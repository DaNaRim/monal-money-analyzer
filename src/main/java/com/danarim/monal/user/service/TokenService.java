package com.danarim.monal.user.service;

import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.User;

/**
 * Service for creating and validating {@link Token}s.
 */
public interface TokenService {

    Token createVerificationToken(User user);

    Token validateVerificationToken(String tokenValue);

    Token createPasswordResetToken(User user);

    Token validatePasswordResetToken(String tokenValue);

}
