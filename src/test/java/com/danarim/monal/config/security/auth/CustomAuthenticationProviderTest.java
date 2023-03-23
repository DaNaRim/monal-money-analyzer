package com.danarim.monal.config.security.auth;

import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationProviderTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final Set<Role> USER_AUTHORITIES = Set.of(new Role(RoleName.ROLE_USER));

    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final Authentication authentication = mock(Authentication.class);
    private final User user = mock(User.class);

    @InjectMocks
    private CustomAuthenticationProvider authProvider;

    @BeforeEach
    void setUp() {
        when(authentication.getName()).thenReturn(USERNAME);
        when(authentication.getCredentials()).thenReturn(PASSWORD);

        when(user.getUsername()).thenReturn(USERNAME);
        when(user.getPassword()).thenReturn(PASSWORD);
        when(user.getRoles()).thenReturn(USER_AUTHORITIES);
        when(user.isEnabled()).thenReturn(true);
        when(user.isAccountNonExpired()).thenReturn(true);
        when(user.isAccountNonLocked()).thenReturn(true);
        when(user.isCredentialsNonExpired()).thenReturn(true);

        when(passwordEncoder.matches(eq(PASSWORD), any()))
                .thenReturn(true);

        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(user);
    }

    @Test
    void authenticate() {
        Authentication resultAuth = authProvider.authenticate(authentication);

        assertFalse(Collections.disjoint(USER_AUTHORITIES, resultAuth.getAuthorities()));
        assertEquals(user, resultAuth.getPrincipal());
        assertEquals(PASSWORD, resultAuth.getCredentials());
    }

    @Test
    void authenticate_NoUsername_UsernameNotFoundException() {
        when(authentication.getName()).thenReturn(null);
        assertThrows(UsernameNotFoundException.class,
                     () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_NullUsername_UsernameNotFoundException() {
        when(authentication.getName()).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                     () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_BlankUsername_UsernameNotFoundException() {
        when(authentication.getName()).thenReturn("    ");

        assertThrows(UsernameNotFoundException.class,
                     () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_NoPassword_BadCredentialsException() {
        when(authentication.getCredentials()).thenReturn(null);
        assertThrows(BadCredentialsException.class,
                     () -> authProvider.authenticate(authentication));

        when(authentication.getCredentials()).thenReturn(new Object());
        assertThrows(BadCredentialsException.class,
                     () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_NullCredentials_BadCredentialsException() {
        when(authentication.getCredentials()).thenReturn(null);
        assertThrows(BadCredentialsException.class,
                     () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_IncorrectCredentials_BadCredentialsException() {
        Object credentials = mock(Object.class);
        when(credentials.toString()).thenReturn(null);

        when(authentication.getCredentials()).thenReturn(credentials);
        assertThrows(BadCredentialsException.class,
                     () -> authProvider.authenticate(authentication));

        when(credentials.toString()).thenReturn("    ");
        assertThrows(BadCredentialsException.class,
                     () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_WrongUsername_UsernameNotFoundException() {
        when(authentication.getName()).thenReturn("wrong");
        when(userDetailsService.loadUserByUsername("wrong"))
                .thenThrow(UsernameNotFoundException.class);

        assertThrows(UsernameNotFoundException.class,
                     () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_WrongPassword_BadCredentialsException() {
        when(passwordEncoder.matches(eq(PASSWORD), any()))
                .thenReturn(false);
        assertThrows(BadCredentialsException.class,
                     () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_UserDisabled_DisabledException() {
        when(user.isEnabled()).thenReturn(false);
        assertThrows(DisabledException.class, () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_UserExpired_AccountExpiredException() {
        when(user.isAccountNonExpired()).thenReturn(false);
        assertThrows(AccountExpiredException.class,
                     () -> authProvider.authenticate(authentication));
    }

    @Test
    void authenticate_UserLocked_LockedException() {
        when(user.isAccountNonLocked()).thenReturn(false);
        assertThrows(LockedException.class, () -> authProvider.authenticate(authentication));
    }

}
