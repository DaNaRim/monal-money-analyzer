package com.danarim.monal.config.security.auth;

import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(UserDao userDao,
                                        @Lazy PasswordEncoder passwordEncoder
    ) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        final String name = authentication.getName();
        final String password = authentication.getCredentials().toString();

        User user = userDao.findByEmail(name);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("User is not enabled");
        }
        if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("User account is expired");
        }
        if (!user.isAccountNonLocked()) {
            throw new LockedException("User account is locked");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Wrong password");
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        user.getRoles().forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority(role.getAuthority())));
        return new UsernamePasswordAuthenticationToken(user, password, grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication == UsernamePasswordAuthenticationToken.class;
    }
}
