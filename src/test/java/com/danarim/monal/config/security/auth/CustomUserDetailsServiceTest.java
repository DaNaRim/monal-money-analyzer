package com.danarim.monal.config.security.auth;

import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomUserDetailsServiceTest {

    private static final String EMAIL = "email";

    private final UserDao userDao = mock(UserDao.class);

    private final CustomUserDetailsService customUserDetailsService = new CustomUserDetailsService(userDao);

    @Test
    void testLoadUserByUsername() {
        User user = new User("1", "1", EMAIL, "1", new Date(), Set.of(new Role(RoleName.ROLE_USER)));

        when(userDao.findByEmail(EMAIL)).thenReturn(user);

        assertEquals(user, customUserDetailsService.loadUserByUsername(EMAIL));
    }

    @Test
    void testUserNotFound() {
        when(userDao.findByEmail(anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(EMAIL));
    }
}
