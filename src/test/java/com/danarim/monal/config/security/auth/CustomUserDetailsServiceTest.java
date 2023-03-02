package com.danarim.monal.config.security.auth;

import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    private static final String EMAIL = "email";

    private final UserDao userDao = mock(UserDao.class);

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername() {
        User user = mock(User.class);

        when(userDao.findByEmailIgnoreCase(EMAIL)).thenReturn(user);

        assertEquals(user, customUserDetailsService.loadUserByUsername(EMAIL));
    }

    @Test
    void loadUserByUsername_UserNotFound_UsernameNotFoundException() {
        when(userDao.findByEmailIgnoreCase(anyString())).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                     () -> customUserDetailsService.loadUserByUsername(EMAIL));
    }

}
