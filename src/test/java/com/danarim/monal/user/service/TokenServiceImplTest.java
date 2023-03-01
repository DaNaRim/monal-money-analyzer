package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.user.persistence.dao.TokenDao;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.TokenType;
import com.danarim.monal.user.persistence.model.User;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    private static final LogCaptor logCaptor = LogCaptor.forClass(TokenServiceImpl.class);

    private final TokenDao tokenDao = mock(TokenDao.class);

    @InjectMocks
    private TokenServiceImpl tokenService;

    @AfterAll
    public static void tearDown() {
        logCaptor.close();
    }

    @AfterEach
    public void clearLogs() {
        logCaptor.clearLogs();
    }

    /*
      Verification token
     */

    @Test
    void createVerificationToken() {
        User user = mock(User.class);
        tokenService.createVerificationToken(user);
        verify(tokenDao).save(any(Token.class));
    }

    @Test
    void createVerificationToken_DelayNotPassed_BadRequestException() {
        User user = mock(User.class);

        when(tokenDao.findLastTokenCreationDate(user, TokenType.VERIFICATION))
                .thenReturn(new Date());

        assertThrows(BadRequestException.class,
                     () -> tokenService.createVerificationToken(user));
    }

    @Test
    void validateVerificationToken() {
        User user = mock(User.class);
        Token verificationToken = new Token(user, TokenType.VERIFICATION);

        when(user.isEnabled()).thenReturn(false);
        when(tokenDao.findByTokenValue(verificationToken.getTokenValue()))
                .thenReturn(verificationToken);

        Token resultToken =
                tokenService.validateVerificationToken(verificationToken.getTokenValue());

        assertEquals(verificationToken, resultToken);
    }

    @Test
    void validateVerificationToken_Invalid_InvalidTokenException() {
        when(tokenDao.findByTokenValue("invalid")).thenReturn(null);

        assertThrows(InvalidTokenException.class,
                     () -> tokenService.validateVerificationToken("invalid"));
    }

    @Test
    void validateVerificationToken_Expired_InvalidTokenException() {
        Token verificationToken = new Token(mock(User.class), TokenType.VERIFICATION);
        verificationToken.setExpirationDate(new Date(0L));

        when(tokenDao.findByTokenValue(verificationToken.getTokenValue()))
                .thenReturn(verificationToken);

        String tokenValue = verificationToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                     () -> tokenService.validateVerificationToken(tokenValue));
    }

    @Test
    void validateVerificationToken_WrongType_InvalidTokenException() {
        Token passwordResetToken = new Token(mock(User.class), TokenType.PASSWORD_RESET);

        when(tokenDao.findByTokenValue(passwordResetToken.getTokenValue())).thenReturn(
                passwordResetToken);

        String tokenValue = passwordResetToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                     () -> tokenService.validateVerificationToken(tokenValue)
        );
    }

    @Test
    void validateVerificationToken_AlreadyUsed_InvalidTokenException() {
        Token verificationToken = new Token(mock(User.class), TokenType.VERIFICATION);
        verificationToken.setUsed();

        when(tokenDao.findByTokenValue(verificationToken.getTokenValue())).thenReturn(
                verificationToken);

        String tokenValue = verificationToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                     () -> tokenService.validateVerificationToken(tokenValue)
        );
    }

    @Test
    void validateVerificationToken_UserEnabled_InvalidTokenException() {
        User user = mock(User.class);
        Token verificationToken = new Token(user, TokenType.VERIFICATION);

        when(user.isEnabled()).thenReturn(true);
        when(tokenDao.findByTokenValue(verificationToken.getTokenValue())).thenReturn(
                verificationToken);

        String tokenValue = verificationToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                     () -> tokenService.validateVerificationToken(tokenValue)
        );
    }

    /*
      Password reset token
     */

    @Test
    void createPasswordResetToken() {
        User user = mock(User.class);
        tokenService.createPasswordResetToken(user);
        verify(tokenDao).save(any(Token.class));
    }

    @Test
    void createPasswordResetToken_DelayNotPassed_BadRequestException() {
        User user = mock(User.class);

        when(tokenDao.findLastTokenCreationDate(user, TokenType.PASSWORD_RESET))
                .thenReturn(new Date());

        assertThrows(BadRequestException.class,
                     () -> tokenService.createPasswordResetToken(user));
    }

    @Test
    void validatePasswordResetToken() {
        User user = mock(User.class);
        Token passwordResetToken = new Token(user, TokenType.PASSWORD_RESET);

        when(tokenDao.findByTokenValue(passwordResetToken.getTokenValue()))
                .thenReturn(passwordResetToken);

        Token resultToken =
                tokenService.validatePasswordResetToken(passwordResetToken.getTokenValue());

        assertEquals(passwordResetToken, resultToken);
    }

    @Test
    void validatePasswordResetToken_Invalid_InvalidTokenException() {
        when(tokenDao.findByTokenValue("invalid")).thenReturn(null);

        assertThrows(InvalidTokenException.class,
                     () -> tokenService.validatePasswordResetToken("invalid"));
    }

    @Test
    void validatePasswordResetToken_AlreadyUsed_InvalidTokenException() {
        Token passwordResetToken = new Token(mock(User.class), TokenType.PASSWORD_RESET);
        passwordResetToken.setUsed();

        when(tokenDao.findByTokenValue(passwordResetToken.getTokenValue()))
                .thenReturn(passwordResetToken);

        String tokenValue = passwordResetToken.getTokenValue();

        assertThrows(InvalidTokenException.class,
                     () -> tokenService.validatePasswordResetToken(tokenValue));
    }

    @Test
    void validatePasswordResetToken_Expired_InvalidTokenException() {
        Token passwordResetToken = new Token(mock(User.class), TokenType.PASSWORD_RESET);
        passwordResetToken.setExpirationDate(new Date(0L));

        when(tokenDao.findByTokenValue(passwordResetToken.getTokenValue()))
                .thenReturn(passwordResetToken);

        String tokenValue = passwordResetToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                     () -> tokenService.validatePasswordResetToken(tokenValue));
    }

    @Test
    void validatePasswordResetToken_WrongType_InvalidTokenException() {
        Token verificationToken = new Token(mock(User.class), TokenType.VERIFICATION);

        when(tokenDao.findByTokenValue(verificationToken.getTokenValue()))
                .thenReturn(verificationToken);

        String tokenValue = verificationToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                     () -> tokenService.validatePasswordResetToken(tokenValue)
        );
    }

    /*
      Other methods
     */

    @Test
    void deleteDeprecatedTokens() {
        when(tokenDao.countTokensByExpirationDateBefore(any(Date.class)))
                .thenReturn(1);

        tokenService.deleteDeprecatedTokens();

        assertThat(logCaptor.getInfoLogs()).hasSize(2);

        verify(tokenDao, times(1))
                .countTokensByExpirationDateBefore(any(Date.class));
        verify(tokenDao, times(1))
                .deleteByExpirationDateBefore(any(Date.class));
    }

    @Test
    void deleteDeprecatedTokens_NoTokens_notDelete() {
        when(tokenDao.countTokensByExpirationDateBefore(any(Date.class)))
                .thenReturn(0);

        tokenService.deleteDeprecatedTokens();

        assertThat(logCaptor.getInfoLogs()).hasSize(2);

        verify(tokenDao, times(1))
                .countTokensByExpirationDateBefore(any(Date.class));
        verify(tokenDao, never())
                .deleteByExpirationDateBefore(any(Date.class));
    }

    @Test
    void deleteDeprecatedTokens_Failed_noException() {
        when(tokenDao.countTokensByExpirationDateBefore(any(Date.class)))
                .thenReturn(1);
        doThrow(RuntimeException.class)
                .when(tokenDao).deleteByExpirationDateBefore(any(Date.class));

        tokenService.deleteDeprecatedTokens();

        assertThat(logCaptor.getInfoLogs()).hasSize(1);
        assertThat(logCaptor.getErrorLogs()).hasSize(1);

        verify(tokenDao, times(1))
                .countTokensByExpirationDateBefore(any(Date.class));
        verify(tokenDao, times(1))
                .deleteByExpirationDateBefore(any(Date.class));
    }

}
