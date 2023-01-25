package com.danarim.monal.config.security.auth;

import com.danarim.monal.failHandler.CustomAuthFailureHandler;
import com.danarim.monal.user.persistence.model.User;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Handles authentication of a user.
 * <br>
 * Exception handles by {@link CustomAuthFailureHandler}
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(UserDetailsService userDetailsService,
                                        PasswordEncoder passwordEncoder
    ) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Handles authentication of a user.
     *
     * @return token for applying to security context
     *
     * @throws AuthenticationException if authentication fails
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        final String name = authentication.getName();
        final Object credentials = authentication.getCredentials();

        if (name == null || name.isBlank()) {
            throw new UsernameNotFoundException("Username is null"); //displayed as user not found
        }
        if (credentials == null || credentials.toString() == null || credentials.toString()
                .isBlank()) {
            throw new BadCredentialsException("Password is null"); //displayed as invalid password
        }
        User user = (User) userDetailsService.loadUserByUsername(name);

        validateUser(user);

        if (!passwordEncoder.matches(credentials.toString(), user.getPassword())) {
            throw new BadCredentialsException("Wrong password");
        }
        return new UsernamePasswordAuthenticationToken(user, credentials, user.getRoles());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == UsernamePasswordAuthenticationToken.class;
    }

    private static void validateUser(User user) {
        if (!user.isEnabled()) {
            throw new DisabledException("User is not enabled");
        }
        if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("User account is expired");
        }
        if (!user.isAccountNonLocked()) {
            throw new LockedException("User account is locked");
        }
    }

}
