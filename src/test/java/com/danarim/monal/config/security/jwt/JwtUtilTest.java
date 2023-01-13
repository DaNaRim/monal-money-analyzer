package com.danarim.monal.config.security.jwt;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private static final LogCaptor logCaptor = LogCaptor.forClass(JwtUtil.class);

    private final JwtTokenDao jwtTokenDao = mock(JwtTokenDao.class);

    @InjectMocks
    private JwtUtil jwtUtil;

    @AfterAll
    public static void tearDown() {
        logCaptor.close();
    }

    @AfterEach
    public void clearLogs() {
        logCaptor.clearLogs();
    }

    @Test
    void deleteDeprecatedJwtTokens() {
        when(jwtTokenDao.countTokensByExpirationDateBefore(any(Date.class)))
                .thenReturn(1);

        jwtUtil.deleteDeprecatedJwtTokens();

        assertThat(logCaptor.getInfoLogs()).hasSize(2);

        verify(jwtTokenDao, times(1))
                .countTokensByExpirationDateBefore(any(Date.class));
        verify(jwtTokenDao, times(1))
                .deleteByExpirationDateBefore(any(Date.class));
    }

    @Test
    void deleteDeprecatedJwtTokens_NoTokens_notDelete() {
        when(jwtTokenDao.countTokensByExpirationDateBefore(any(Date.class)))
                .thenReturn(0);

        jwtUtil.deleteDeprecatedJwtTokens();

        assertThat(logCaptor.getInfoLogs()).hasSize(2);

        verify(jwtTokenDao, times(1))
                .countTokensByExpirationDateBefore(any(Date.class));
        verify(jwtTokenDao, never())
                .deleteByExpirationDateBefore(any(Date.class));
    }

    @Test
    void deleteDeprecatedJwtTokens_Failed_noException() {
        when(jwtTokenDao.countTokensByExpirationDateBefore(any(Date.class)))
                .thenReturn(1);

        doThrow(RuntimeException.class)
                .when(jwtTokenDao).deleteByExpirationDateBefore(any(Date.class));

        jwtUtil.deleteDeprecatedJwtTokens();

        assertThat(logCaptor.getInfoLogs()).hasSize(1);
        assertThat(logCaptor.getErrorLogs()).hasSize(1);

        verify(jwtTokenDao, times(1))
                .countTokensByExpirationDateBefore(any(Date.class));
        verify(jwtTokenDao, times(1))
                .deleteByExpirationDateBefore(any(Date.class));
    }
}
