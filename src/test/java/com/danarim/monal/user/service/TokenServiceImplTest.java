package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.user.persistence.dao.TokenDao;
import com.danarim.monal.user.persistence.model.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    private static final MockedStatic<LogFactory> loggerFactoryMock = mockStatic(LogFactory.class);
    private static final Log logger = mock(Log.class);

    private final TokenDao tokenDao = mock(TokenDao.class);

    @InjectMocks
    private TokenServiceImpl tokenService;

    @BeforeAll
    public static void beforeClass() {
        loggerFactoryMock.when(() -> LogFactory.getLog(any(Class.class))).thenReturn(logger);
    }

    @AfterAll
    static void afterAll() {
        loggerFactoryMock.close();
    }

    @BeforeEach
    void setUp() {
        reset(logger);
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
    void validateVerificationToken() {
        User user = new User(
                "test", "test",
                "userEmail", "password",
                new Date(), Set.of(new Role(RoleName.ROLE_USER))
        );
        Token verificationToken = new Token(user, TokenType.VERIFICATION);

        when(tokenDao.findByTokenValue(verificationToken.getTokenValue())).thenReturn(verificationToken);

        Token resultToken = tokenService.validateVerificationToken(verificationToken.getTokenValue());

        assertEquals(verificationToken, resultToken);
    }

    @Test
    void validateVerificationTokenInvalid() {
        when(tokenDao.findByTokenValue("invalid")).thenReturn(null);

        assertThrows(InvalidTokenException.class, () -> tokenService.validateVerificationToken("invalid"));
    }

    @Test
    void validateVerificationTokenExpired() {
        Token verificationToken = new Token(mock(User.class), TokenType.VERIFICATION);
        verificationToken.setExpiryDate(new Date(0L));

        when(tokenDao.findByTokenValue(verificationToken.getTokenValue())).thenReturn(verificationToken);

        String tokenValue = verificationToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                () -> tokenService.validateVerificationToken(tokenValue)
        );
    }

    @Test
    void validateVerificationTokenWrongType() {
        Token passwordResetToken = new Token(mock(User.class), TokenType.PASSWORD_RESET);

        when(tokenDao.findByTokenValue(passwordResetToken.getTokenValue())).thenReturn(passwordResetToken);

        String tokenValue = passwordResetToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                () -> tokenService.validateVerificationToken(tokenValue)
        );
    }

    @Test
    void validateVerificationTokenUserEnabled() {
        User user = new User(
                "test", "test",
                "userEmail", "password",
                new Date(), Set.of(new Role(RoleName.ROLE_USER))
        );
        user.setEmailVerified(true);

        Token verificationToken = new Token(user, TokenType.VERIFICATION);
        when(tokenDao.findByTokenValue(verificationToken.getTokenValue())).thenReturn(verificationToken);

        String tokenValue = verificationToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                () -> tokenService.validateVerificationToken(tokenValue)
        );
    }

    /*
      Password reset token
     */

    @Test
    void testCreatePasswordResetToken() {
        User user = mock(User.class);
        tokenService.createPasswordResetToken(user);
        verify(tokenDao).save(any(Token.class));
    }

    @Test
    void testValidatePasswordResetToken() {
        User user = new User(
                "test", "test",
                "userEmail", "password",
                new Date(), Set.of(new Role(RoleName.ROLE_USER))
        );
        Token passwordResetToken = new Token(user, TokenType.PASSWORD_RESET);

        when(tokenDao.findByTokenValue(passwordResetToken.getTokenValue())).thenReturn(passwordResetToken);

        Token resultToken = tokenService.validatePasswordResetToken(passwordResetToken.getTokenValue());

        assertEquals(passwordResetToken, resultToken);
    }

    @Test
    void testValidatePasswordResetTokenInvalid() {
        when(tokenDao.findByTokenValue("invalid")).thenReturn(null);

        assertThrows(InvalidTokenException.class, () -> tokenService.validatePasswordResetToken("invalid"));
    }

    @Test
    void testValidatePasswordResetTokenExpired() {
        Token passwordResetToken = new Token(mock(User.class), TokenType.PASSWORD_RESET);
        passwordResetToken.setExpiryDate(new Date(0L));

        when(tokenDao.findByTokenValue(passwordResetToken.getTokenValue())).thenReturn(passwordResetToken);

        String tokenValue = passwordResetToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                () -> tokenService.validatePasswordResetToken(tokenValue)
        );
    }

    @Test
    void testValidatePasswordResetTokenWrongType() {
        Token verificationToken = new Token(mock(User.class), TokenType.VERIFICATION);

        when(tokenDao.findByTokenValue(verificationToken.getTokenValue())).thenReturn(verificationToken);

        String tokenValue = verificationToken.getTokenValue();
        assertThrows(InvalidTokenException.class,
                () -> tokenService.validatePasswordResetToken(tokenValue)
        );
    }

    /*
      Other methods
     */

    @Test
    void deleteToken() {
        Token token = mock(Token.class);
        tokenService.deleteToken(token);
        verify(tokenDao).delete(token);
    }

    @Test
    @DisplayName("Test scheduled task to delete deprecated tokens")
    void testDeleteDeprecatedTokens() {
        when(tokenDao.countTokensByExpiryDateBefore(any(Date.class))).thenReturn(1);

        tokenService.deleteDeprecatedTokens();

        verify(tokenDao, times(1)).countTokensByExpiryDateBefore(any(Date.class));
        verify(tokenDao, times(1)).deleteByExpiryDateBefore(any(Date.class));

        verify(logger, times(2)).info(anyString());
    }

    @Test
    @DisplayName("Test scheduled task to delete deprecated tokens when no tokens are deprecated")
    void testDeleteDeprecatedTokensNoTokens() {
        when(tokenDao.countTokensByExpiryDateBefore(any(Date.class))).thenReturn(0);

        tokenService.deleteDeprecatedTokens();

        verify(tokenDao, times(1)).countTokensByExpiryDateBefore(any(Date.class));
        verify(tokenDao, never()).deleteByExpiryDateBefore(any(Date.class));

        verify(logger, times(2)).info(anyString());
    }

    @Test
    @DisplayName("Test scheduled task to delete deprecated tokens when exception occurs")
    void testDeleteDeprecatedTokensFailed() {
        when(tokenDao.countTokensByExpiryDateBefore(any(Date.class))).thenReturn(1);
        doThrow(RuntimeException.class).when(tokenDao).deleteByExpiryDateBefore(any(Date.class));

        //expect no exception
        tokenService.deleteDeprecatedTokens();

        verify(tokenDao, times(1)).countTokensByExpiryDateBefore(any(Date.class));
        verify(tokenDao, times(1)).deleteByExpiryDateBefore(any(Date.class));

        verify(logger, times(1)).info(anyString());
        verify(logger, times(1)).error(anyString(), any(RuntimeException.class));
    }
}
