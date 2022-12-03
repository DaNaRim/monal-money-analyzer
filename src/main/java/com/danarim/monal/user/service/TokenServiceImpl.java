package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.user.persistence.dao.TokenDao;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.TokenType;
import com.danarim.monal.user.persistence.model.User;
import org.springframework.stereotype.Service;

/**
 * Service for working with not auth tokens.
 */
@Service
public class TokenServiceImpl implements TokenService {

    /**
     * @see InvalidTokenException expectClientActionCode field in InvalidTokenException class
     */
    private static final String CLIENT_ACTION_TOKEN_VERIFICATION_RESEND = "token.verification.resend";

    private final TokenDao tokenDao;

    public TokenServiceImpl(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    /**
     * Create a new token verification token and save it in the database.
     *
     * @param user user to create token for
     * @return verification token
     */
    @Override
    public Token createVerificationToken(User user) {
        return tokenDao.save(new Token(user, TokenType.VERIFICATION));
    }

    /**
     * Validate verification token.
     *
     * @param tokenValue token value
     * @return Token object with the given value
     * @throws InvalidTokenException if token is not found, wrong type, expired or user is already activated
     */
    @Override
    public Token validateVerificationToken(String tokenValue) {
        Token verificationToken = tokenDao.findByTokenValue(tokenValue);

        if (verificationToken == null) {
            throw new InvalidTokenException("invalidToken",
                    "validation.token.invalid", null,
                    CLIENT_ACTION_TOKEN_VERIFICATION_RESEND
            );
        }
        boolean isUserEnabled = verificationToken.getUser().isEnabled();

        if (verificationToken.getTokenType() != TokenType.VERIFICATION) {
            String clientAction = isUserEnabled ? null : CLIENT_ACTION_TOKEN_VERIFICATION_RESEND;

            throw new InvalidTokenException("invalidToken",
                    "validation.token.wrong-type", new Object[]{TokenType.VERIFICATION},
                    clientAction);
        }
        if (verificationToken.isExpired()) {
            String clientAction = isUserEnabled ? null : CLIENT_ACTION_TOKEN_VERIFICATION_RESEND;

            throw new InvalidTokenException("tokenExpired", "validation.token.expired", null, clientAction);
        }
        if (verificationToken.getUser().isEnabled()) {
            throw new InvalidTokenException("userAlreadyEnable", "validation.token.user.enabled", null, null);
        }
        return verificationToken;
    }

    @Override
    public void deleteToken(Token token) {
        tokenDao.delete(token);
    }
}
