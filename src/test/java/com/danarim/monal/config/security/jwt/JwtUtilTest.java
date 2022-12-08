package com.danarim.monal.config.security.jwt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private static final MockedStatic<LogFactory> loggerFactoryMock = mockStatic(LogFactory.class);
    private static final Log logger = mock(Log.class);

    private final JwtTokenDao jwtTokenDao = mock(JwtTokenDao.class);

    @InjectMocks
    private JwtUtil jwtUtil;

    @BeforeAll
    public static void beforeClass() {
        loggerFactoryMock.when(() -> LogFactory.getLog(any(Class.class))).thenReturn(logger);
    }

    @AfterAll
    static void afterAll() {
        loggerFactoryMock.close();
    }

    @BeforeEach
    void setUp() {
        reset(logger);
    }

    @Test
    @DisplayName("Test scheduled task to delete deprecated jwt tokens")
    void deleteDeprecatedJwtTokens() {
        when(jwtTokenDao.countTokensByExpirationDateBefore(any(Date.class))).thenReturn(1);

        jwtUtil.deleteDeprecatedJwtTokens();

        verify(jwtTokenDao, times(1)).countTokensByExpirationDateBefore(any(Date.class));
        verify(jwtTokenDao, times(1)).deleteByExpirationDateBefore(any(Date.class));

        verify(logger, times(2)).info(anyString());
    }

    @Test
    @DisplayName("Test scheduled task to delete deprecated jwt tokens when no tokens are deprecated")
    void testDeleteDeprecatedTokensNoTokens() {
        when(jwtTokenDao.countTokensByExpirationDateBefore(any(Date.class))).thenReturn(0);

        jwtUtil.deleteDeprecatedJwtTokens();

        verify(jwtTokenDao, times(1)).countTokensByExpirationDateBefore(any(Date.class));
        verify(jwtTokenDao, never()).deleteByExpirationDateBefore(any(Date.class));

        verify(logger, times(2)).info(anyString());
    }

    @Test
    @DisplayName("Test scheduled task to delete deprecated jwt tokens when exception occurs")
    void testDeleteDeprecatedTokensFailed() {
        when(jwtTokenDao.countTokensByExpirationDateBefore(any(Date.class))).thenReturn(1);
        doThrow(RuntimeException.class).when(jwtTokenDao).deleteByExpirationDateBefore(any(Date.class));

        //expect no exception
        jwtUtil.deleteDeprecatedJwtTokens();

        verify(jwtTokenDao, times(1)).countTokensByExpirationDateBefore(any(Date.class));
        verify(jwtTokenDao, times(1)).deleteByExpirationDateBefore(any(Date.class));

        verify(logger, times(1)).info(anyString());
        verify(logger, times(1)).error(anyString(), any(RuntimeException.class));
    }
}
