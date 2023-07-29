package com.danarim.monal.config.security.auth;

import com.danarim.monal.exceptions.InternalServerException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUtilTest {

    private static final MockedStatic<SecurityContextHolder> securityContextHolder =
            mockStatic(SecurityContextHolder.class);

    private final SecurityContext securityContext = mock(SecurityContext.class);
    private final Authentication authentication = mock(Authentication.class);

    @AfterAll
    static void afterAll() {
        securityContextHolder.close();
    }

    @BeforeEach
    void setUp() {
        when(SecurityContextHolder.getContext()).thenReturn(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void createAuthUtilInstance() throws NoSuchMethodException {
        Constructor<AuthUtil> constructor = AuthUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(
                InvocationTargetException.class,
                constructor::newInstance
        );
    }

    @Test
    void getLoggedUserId() {
        when(authentication.getPrincipal()).thenReturn("1");

        assertEquals(1L, AuthUtil.getLoggedUserId());
    }

    @Test
    void getLoggedUserId_principalIsNotString_InternalServerException() {
        when(authentication.getPrincipal()).thenReturn(1);

        InternalServerException exception = assertThrows(
                InternalServerException.class,
                AuthUtil::getLoggedUserId
        );
        assertNotNull(exception.getMessage());
    }

    @Test
    void getLoggedUserId_principalIsNotStringNumber_InternalServerException() {
        when(authentication.getPrincipal()).thenReturn("a");

        InternalServerException exception = assertThrows(
                InternalServerException.class,
                AuthUtil::getLoggedUserId
        );
        assertNotNull(exception.getMessage());
    }

}
