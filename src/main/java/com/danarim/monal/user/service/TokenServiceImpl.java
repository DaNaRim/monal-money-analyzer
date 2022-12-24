package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.user.persistence.dao.TokenDao;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.TokenType;
import com.danarim.monal.user.persistence.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Service for working with not auth tokens.
 */
@Service
public class TokenServiceImpl implements TokenService {

    /**
     * Rule for scheduled task. Delete all tokens that expired and stored in database for this count of days.
     */
    private static final int DELETE_TOKENS_THAT_EXPIRED_BEFORE_DAYS = 7;
    /**
     * Delay for scheduled task that delete tokens.
     */
    private static final long DELETE_TOKENS_DELAY_IN_DAYS = 1L;


    private static final Log logger = LogFactory.getLog(TokenServiceImpl.class);

    private final TokenDao tokenDao;

    public TokenServiceImpl(TokenDao tokenDao) {
        this.tokenDao = tokenDao;
    }

    /*
      Verification token
     */

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
     * @throws InvalidTokenException if token is not found, wrong type, expired, already used or user is already activated
     */
    @Override
    public Token validateVerificationToken(String tokenValue) {
        Token verificationToken = tokenDao.findByTokenValue(tokenValue);

        if (verificationToken == null) {
            throw new InvalidTokenException("token not found", "validation.token.invalid", null);
        }
        if (verificationToken.getTokenType() != TokenType.VERIFICATION) {
            throw new InvalidTokenException(
                    "wrong token type expected: " + TokenType.VERIFICATION + " actual: " + verificationToken.getTokenType(),
                    "validation.token.wrong-type",
                    new Object[]{TokenType.VERIFICATION, verificationToken.getTokenType()}
            );
        }
        if (verificationToken.isUsed()) {
            throw new InvalidTokenException("token already used", "validation.token.used", null);
        }
        if (verificationToken.isExpired()) {
            throw new InvalidTokenException("token expired", "validation.token.expired", null);
        }
        if (verificationToken.getUser().isEnabled()) {
            throw new InvalidTokenException("user already enable", "validation.token.user.enabled", null);
        }
        return verificationToken;
    }

    /*
      Password reset token
     */

    /**
     * Create a new password reset token and save it in the database.
     *
     * @param user user to create token for
     * @return password reset token
     */
    @Override
    public Token createPasswordResetToken(User user) {
        return tokenDao.save(new Token(user, TokenType.PASSWORD_RESET));
    }

    /**
     * @param tokenValue token value
     * @return Token object with the given value
     * @throws InvalidTokenException if token is not found, wrong type or expired
     */
    @Override
    public Token validatePasswordResetToken(String tokenValue) {
        Token passwordResetToken = tokenDao.findByTokenValue(tokenValue);

        if (passwordResetToken == null) {
            throw new InvalidTokenException("token not found", "validation.token.invalid", null);
        }
        if (passwordResetToken.getTokenType() != TokenType.PASSWORD_RESET) {
            throw new InvalidTokenException(
                    "wrong token type expected: " + TokenType.PASSWORD_RESET + " actual: " + passwordResetToken.getTokenType(),
                    "validation.token.wrong-type",
                    new Object[]{TokenType.PASSWORD_RESET, passwordResetToken.getTokenType()}
            );
        }
        if (passwordResetToken.isUsed()) {
            throw new InvalidTokenException("token already used", "validation.token.used", null);
        }
        if (passwordResetToken.isExpired()) {
            throw new InvalidTokenException("token expired", "validation.token.expired", null);
        }
        return passwordResetToken;
    }

    /*
      Other methods
     */

    /**
     * Deletes the given token from the database.
     *
     * @param token token to delete
     */
    @Override
    public void deleteToken(Token token) {
        tokenDao.delete(token);
    }

    /**
     * Delete all tokens that expired before given date.
     * <br>
     * Token becomes deprecated when it is expired and time {@link #DELETE_TOKENS_THAT_EXPIRED_BEFORE_DAYS} passed.
     */
    @Scheduled(fixedRate = DELETE_TOKENS_DELAY_IN_DAYS, timeUnit = TimeUnit.DAYS)
    protected void deleteDeprecatedTokens() {
        logger.info("Scheduled task: delete deprecated tokens started");

        //Get date before which tokens will be deleted
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -DELETE_TOKENS_THAT_EXPIRED_BEFORE_DAYS);

        Date removeBeforeDate = calendar.getTime();
        try {
            int tokensToDelete = tokenDao.countTokensByExpiryDateBefore(removeBeforeDate);

            if (tokensToDelete == 0) {
                logger.info("Scheduled task: delete deprecated tokens finished. No tokens to delete");
                return;
            }
            tokenDao.deleteByExpiryDateBefore(removeBeforeDate);
            logger.info("Scheduled task: delete deprecated tokens finished. " + tokensToDelete + " tokens deleted");
        } catch (RuntimeException e) {
            logger.error("Scheduled task: delete deprecated tokens failed", e);
        }
    }
}
