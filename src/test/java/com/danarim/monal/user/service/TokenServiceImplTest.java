package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.user.persistence.dao.TokenDao;
import com.danarim.monal.user.persistence.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    private final TokenDao tokenDao = mock(TokenDao.class);

    @InjectMocks
    private TokenServiceImpl tokenService;

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

    @Test
    void deleteToken() {
        Token token = mock(Token.class);
        tokenService.deleteToken(token);
        verify(tokenDao).delete(token);
    }
}
