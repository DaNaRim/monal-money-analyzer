package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.AlreadyExistsException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.GenericErrorType;
import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.user.persistence.dao.RoleDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.*;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.user.web.dto.ResetPasswordDto;
import com.danarim.monal.util.MailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Set;

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

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testRegisterUser() {
        when(userDao.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationDto registrationDto = new RegistrationDto(
                "test", "test",
                "password", "password",
                "email"
        );

        registrationService.registerNewUserAccount(registrationDto);

        verify(userDao).existsByEmailIgnoreCase(registrationDto.email());
        verify(userDao).save(any(User.class));
        verify(passwordEncoder).encode(registrationDto.password());
        verify(roleDao).findByRoleName(RoleName.ROLE_USER);
    }

    @Test
    void testRegisterUserWithExistEmail() {
        when(userDao.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        RegistrationDto registrationDto = new RegistrationDto(
                "test", "test",
                "password", "password",
                "existsEmail"
        );

        AlreadyExistsException e = assertThrows(AlreadyExistsException.class,
                () -> registrationService.registerNewUserAccount(registrationDto));

        assertEquals("email", e.getField());
        assertEquals(GenericErrorType.FIELD_VALIDATION_ERROR, e.getErrorType());
        assertNotNull(e.getMessageCode());

        verify(userDao).existsByEmailIgnoreCase(registrationDto.email());
    }

    @Test
    void testConfirmRegistration() {
        User user = new User(
                "test", "test",
                "email", "password", new Date(), Set.of(new Role(RoleName.ROLE_USER))
        );
        Token verificationToken = new Token(user, TokenType.VERIFICATION);
        when(tokenService.validateVerificationToken("token")).thenReturn(verificationToken);

        registrationService.confirmRegistration("token");

        verify(tokenService).validateVerificationToken("token");
        verify(tokenService).deleteToken(verificationToken);
        assertTrue(user.isEnabled());
    }

    @Test
    void testConfirmRegistrationFails() {
        doThrow(InvalidTokenException.class).when(tokenService).validateVerificationToken("token");

        assertThrows(InvalidTokenException.class, () -> registrationService.confirmRegistration("token"));

        verify(tokenService).validateVerificationToken("token");
        verify(tokenService, never()).deleteToken(any(Token.class));
    }

    @Test
    void testResendVerificationToken() {
        User user = new User(
                "test", "test",
                "email", "password", new Date(), Set.of(new Role(RoleName.ROLE_USER))
        );
        when(userDao.findByEmailIgnoreCase("email")).thenReturn(user);
        Token verificationToken = new Token(user, TokenType.VERIFICATION);
        when(tokenService.createVerificationToken(user)).thenReturn(verificationToken);

        registrationService.resendVerificationEmail(user.getEmail());

        verify(userDao).findByEmailIgnoreCase(user.getEmail());
        verify(tokenService).createVerificationToken(user);
        verify(mailUtil).sendVerificationEmail(verificationToken.getTokenValue(), user.getEmail());
    }

    @Test
    void testResendVerificationTokenUserNotFound() {
        when(userDao.findByEmailIgnoreCase("email")).thenReturn(null);

        assertThrows(BadRequestException.class, () -> registrationService.resendVerificationEmail("email"));

        verify(userDao).findByEmailIgnoreCase("email");
        verify(tokenService, never()).createVerificationToken(any(User.class));
        verify(mailUtil, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void testResendVerificationTokenUserEnabled() {
        User user = new User(
                "test", "test",
                "email", "password", new Date(), Set.of(new Role(RoleName.ROLE_USER))
        );
        user.setEmailVerified(true);

        when(userDao.findByEmailIgnoreCase(user.getEmail())).thenReturn(user);

        assertThrows(BadRequestException.class, () -> registrationService.resendVerificationEmail("email"));

        verify(userDao).findByEmailIgnoreCase(user.getEmail());
        verify(tokenService, never()).createVerificationToken(any(User.class));
        verify(mailUtil, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void testResetPassword() {
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
    void testResetPasswordUserNotFound() {
        when(userDao.findByEmailIgnoreCase("email")).thenReturn(null);

        assertThrows(BadRequestException.class, () -> registrationService.resetPassword("email"));

        verify(userDao).findByEmailIgnoreCase("email");
        verify(tokenService, never()).createPasswordResetToken(any(User.class));
        verify(mailUtil, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void testValidatePasswordResetToken() {
        registrationService.validatePasswordResetToken("token");

        verify(tokenService).validatePasswordResetToken("token");
    }

    @Test
    void testUpdateForgottenPassword() {
        ResetPasswordDto resetPasswordDto = mock(ResetPasswordDto.class);
        Token resetToken = mock(Token.class);
        User user = mock(User.class);

        when(tokenService.validatePasswordResetToken("token")).thenReturn(resetToken);
        when(resetToken.getUser()).thenReturn(user);
        when(resetPasswordDto.password()).thenReturn("password");
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        User result = registrationService.updateForgottenPassword(resetPasswordDto, "token");

        verify(tokenService).validatePasswordResetToken("token");
        verify(passwordEncoder).encode("password");
        verify(user).setPassword("encodedPassword");
        verify(tokenService).deleteToken(resetToken);

        assertEquals(user, result);
    }
}
