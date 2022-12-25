package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.user.persistence.dao.RoleDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.TokenType;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.user.web.dto.ResetPasswordDto;
import com.danarim.monal.util.MailUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    private final UserDao userDao = mock(UserDao.class);
    private final RoleDao roleDao = mock(RoleDao.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final MailUtil mailUtil = mock(MailUtil.class);

    @InjectMocks
    private RegistrationServiceImpl registrationService;

    @Test
    void registerNewUserAccount() {
        RegistrationDto registrationDto = new RegistrationDto(
                "test", "test",
                "password", "password",
                "email"
        );

        when(userDao.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userDao.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registrationService.registerNewUserAccount(registrationDto);

        verify(userDao).existsByEmailIgnoreCase(registrationDto.email());
        verify(userDao).save(any(User.class));
        verify(passwordEncoder).encode(registrationDto.password());
        verify(roleDao).findByRoleName(RoleName.ROLE_USER);
    }

    @Test
    void registerNewUserAccount_EmailExists_BadFieldException() {
        RegistrationDto registrationDto = new RegistrationDto(
                "test", "test",
                "password", "password",
                "existsEmail"
        );

        when(userDao.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        BadFieldException resultException = assertThrows(BadFieldException.class,
                () -> registrationService.registerNewUserAccount(registrationDto));

        assertEquals("email", resultException.getField());
        assertNotNull(resultException.getMessageCode());

        verify(userDao).existsByEmailIgnoreCase(registrationDto.email());
    }

    @Test
    void confirmRegistration() {
        User user = mock(User.class);
        Token verificationToken = new Token(user, TokenType.VERIFICATION);

        when(tokenService.validateVerificationToken("token"))
                .thenReturn(verificationToken);

        registrationService.confirmRegistration("token");

        assertTrue(verificationToken.isUsed());

        verify(user).setEmailVerified(true);
        verify(tokenService).validateVerificationToken("token");
    }

    @Test
    void resendVerificationEmail() {
        User user = mock(User.class);
        Token verificationToken = new Token(user, TokenType.VERIFICATION);

        when(user.isEnabled()).thenReturn(false);
        when(userDao.findByEmailIgnoreCase("email")).thenReturn(user);
        when(tokenService.createVerificationToken(user)).thenReturn(verificationToken);

        registrationService.resendVerificationEmail("email");

        verify(userDao).findByEmailIgnoreCase("email");
        verify(tokenService).createVerificationToken(user);
        verify(mailUtil).sendVerificationEmail(verificationToken.getTokenValue(), "email");
    }

    @Test
    void resendVerificationEmail_UserNotFound_BadFieldException() {
        when(userDao.findByEmailIgnoreCase("email")).thenReturn(null);

        assertThrows(BadFieldException.class,
                () -> registrationService.resendVerificationEmail("email"));

        verify(userDao).findByEmailIgnoreCase("email");
        verify(tokenService, never()).createVerificationToken(any(User.class));
        verify(mailUtil, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void resendVerificationEmail_UserEnabled_BadRequestException() {
        User user = mock(User.class);

        when(user.isEnabled()).thenReturn(true);
        when(userDao.findByEmailIgnoreCase("email")).thenReturn(user);

        assertThrows(BadRequestException.class,
                () -> registrationService.resendVerificationEmail("email"));

        verify(userDao).findByEmailIgnoreCase("email");
        verify(tokenService, never()).createVerificationToken(any(User.class));
        verify(mailUtil, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void resetPassword() {
        User user = mock(User.class);
        Token resetToken = new Token(user, TokenType.PASSWORD_RESET);

        when(userDao.findByEmailIgnoreCase("email")).thenReturn(user);
        when(tokenService.createPasswordResetToken(user)).thenReturn(resetToken);

        registrationService.resetPassword("email");

        verify(userDao).findByEmailIgnoreCase("email");
        verify(tokenService).createPasswordResetToken(any(User.class));
        verify(mailUtil).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_UserNotFound_BadFieldException() {
        when(userDao.findByEmailIgnoreCase("email")).thenReturn(null);

        assertThrows(BadFieldException.class,
                () -> registrationService.resetPassword("email"));

        verify(userDao).findByEmailIgnoreCase("email");
        verify(tokenService, never()).createPasswordResetToken(any(User.class));
        verify(mailUtil, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void updateForgottenPassword() {
        ResetPasswordDto resetPasswordDto = mock(ResetPasswordDto.class);
        User user = mock(User.class);
        Token resetToken = new Token(user, TokenType.PASSWORD_RESET);

        when(resetPasswordDto.password()).thenReturn("password");
        when(tokenService.validatePasswordResetToken("token")).thenReturn(resetToken);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        User resultUser = registrationService.updateForgottenPassword(resetPasswordDto, "token");

        assertEquals(user, resultUser);
        assertTrue(resetToken.isUsed());

        verify(tokenService).validatePasswordResetToken("token");
        verify(passwordEncoder).encode("password");
        verify(user).setPassword("encodedPassword");
    }

    @Test
    void updateForgottenPassword_SameAsOld_BadRequestException() {
        ResetPasswordDto resetPasswordDto = mock(ResetPasswordDto.class);
        User user = mock(User.class);
        Token resetToken = new Token(user, TokenType.PASSWORD_RESET);

        when(user.getPassword()).thenReturn("password");
        when(resetPasswordDto.password()).thenReturn("password");
        when(tokenService.validatePasswordResetToken("token")).thenReturn(resetToken);
        when(passwordEncoder.matches("password", "password")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> registrationService.updateForgottenPassword(resetPasswordDto, "token"));

        verify(tokenService).validatePasswordResetToken("token");
        verify(passwordEncoder).matches("password", "password");
        verify(user, never()).setPassword(anyString());
    }
}
