package com.danarim.monal.config.security.auth;

import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomAuthenticationProviderTest {

    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final Set<Role> USER_AUTHORITIES = Set.of(new Role(RoleName.ROLE_USER));

    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final CustomAuthenticationProvider authProvider = new CustomAuthenticationProvider(
            userDetailsService,
            passwordEncoder
    );

    private final Authentication authentication = mock(Authentication.class);
    private final User user = mock(User.class);

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

        when(passwordEncoder.matches(eq(PASSWORD), any())).thenReturn(true);

        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(user);
    }

    @Test
    void testAuthenticate() {
        Authentication resultAuth = authProvider.authenticate(authentication);

        assertFalse(Collections.disjoint(USER_AUTHORITIES, resultAuth.getAuthorities()));
        assertEquals(user, resultAuth.getPrincipal());
        assertEquals(PASSWORD, resultAuth.getCredentials());
    }

    @Test
    void testNoUsername() {
        when(authentication.getName()).thenReturn(null);
        assertThrows(UsernameNotFoundException.class, () -> authProvider.authenticate(authentication));
    }

    @Test
    void testNoPassword() {
        when(authentication.getCredentials()).thenReturn(null);
        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(authentication));

        when(authentication.getCredentials()).thenReturn(new Object());
        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(authentication));
    }

    @Test
    void testWrongUsername() {
        when(authentication.getName()).thenReturn(USERNAME + "wrong");
        when(userDetailsService.loadUserByUsername(USERNAME + "wrong")).thenThrow(new UsernameNotFoundException(""));

        assertThrows(UsernameNotFoundException.class, () -> authProvider.authenticate(authentication));
    }

    @Test
    void testWrongPassword() {
        when(passwordEncoder.matches(eq(PASSWORD), any())).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(authentication));
    }

    @Test
    void testDisabled() {
        when(user.isEnabled()).thenReturn(false);
        assertThrows(DisabledException.class, () -> authProvider.authenticate(authentication));
    }

    @Test
    void testExpired() {
        when(user.isAccountNonExpired()).thenReturn(false);
        assertThrows(AccountExpiredException.class, () -> authProvider.authenticate(authentication));
    }

    @Test
    void testLocked() {
        when(user.isAccountNonLocked()).thenReturn(false);
        assertThrows(LockedException.class, () -> authProvider.authenticate(authentication));
    }
}
