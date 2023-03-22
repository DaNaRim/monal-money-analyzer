package com.danarim.monal.failhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//Unit test because can`t mock User in IT because of stub methods in it
@ExtendWith(MockitoExtension.class)
class CustomAuthFailureHandlerTest {

    private static final HttpServletRequest request = mock(HttpServletRequest.class);
    private static final HttpServletResponse response = mock(HttpServletResponse.class);

    private static final MessageSource messages = mock(MessageSource.class);
    private static final LocaleResolver localeResolver = mock(LocaleResolver.class);

    @Captor
    private ArgumentCaptor<List<ErrorResponse>> errorCaptor;

    @InjectMocks
    private CustomAuthFailureHandler failHandler;

    @BeforeAll
    static void beforeAll() throws IOException {
        when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
        when(localeResolver.resolveLocale(request)).thenReturn(Locale.ENGLISH);
    }

    @BeforeEach
    void setUp() {
        reset(response);
    }

    @Test
    void onAuthenticationFailure_UsernameNotFoundException() throws IOException {
        when(messages.getMessage(eq("validation.auth.notFound"), any(), any()))
                .thenReturn("auth.notFound");

        try (MockedConstruction<ObjectMapper> mapper = mockConstruction(ObjectMapper.class)) {
            failHandler.onAuthenticationFailure(request, response,
                                                new UsernameNotFoundException("Username not found")
            );
            verify(response).setStatus(SC_UNAUTHORIZED);
            ObjectMapper mockedMapper = mapper.constructed().get(0);

            verify(mockedMapper).writeValue(eq(response.getOutputStream()), errorCaptor.capture());

            List<ErrorResponse> errors = errorCaptor.getValue();
            assertEquals(1, errors.size());

            ErrorResponse error = errors.get(0);
            assertEquals("username", error.fieldName());
            assertEquals("fieldValidationError", error.type());
            assertEquals("auth.notFound", error.message());
            assertEquals("validation.auth.notFound", error.errorCode());
        }
    }

    @Test
    void onAuthenticationFailure_BadCredentialsException() throws IOException {
        when(messages.getMessage(eq("validation.auth.badCredentials"), any(), any()))
                .thenReturn("auth.badCredentials");

        try (MockedConstruction<ObjectMapper> mapper = mockConstruction(ObjectMapper.class)) {
            failHandler.onAuthenticationFailure(request, response,
                                                new BadCredentialsException("Bad credentials")
            );
            verify(response).setStatus(SC_UNAUTHORIZED);
            ObjectMapper mockedMapper = mapper.constructed().get(0);

            verify(mockedMapper).writeValue(eq(response.getOutputStream()), errorCaptor.capture());

            List<ErrorResponse> errors = errorCaptor.getValue();
            assertEquals(1, errors.size());

            ErrorResponse error = errors.get(0);
            assertEquals("password", error.fieldName());
            assertEquals("fieldValidationError", error.type());
            assertEquals("auth.badCredentials", error.message());
            assertEquals("validation.auth.badCredentials", error.errorCode());
        }
    }

    @Test
    void onAuthenticationFailure_DisabledException() throws IOException {
        when(messages.getMessage(eq("validation.auth.disabled"), any(), any()))
                .thenReturn("auth.disabled");

        try (MockedConstruction<ObjectMapper> mapper = mockConstruction(ObjectMapper.class)) {
            failHandler.onAuthenticationFailure(request, response,
                                                new DisabledException("Account disabled")
            );
            verify(response).setStatus(SC_UNAUTHORIZED);
            ObjectMapper mockedMapper = mapper.constructed().get(0);

            verify(mockedMapper).writeValue(eq(response.getOutputStream()), errorCaptor.capture());

            List<ErrorResponse> errors = errorCaptor.getValue();
            assertEquals(1, errors.size());

            ErrorResponse error = errors.get(0);
            assertEquals("globalError", error.fieldName());
            assertEquals("globalError", error.type());
            assertEquals("auth.disabled", error.message());
            assertEquals("validation.auth.disabled", error.errorCode());
        }
    }

    @Test
    void onAuthenticationFailure_LockedException() throws IOException {
        when(messages.getMessage(eq("validation.auth.blocked"), any(), any()))
                .thenReturn("auth.blocked");

        try (MockedConstruction<ObjectMapper> mapper = mockConstruction(ObjectMapper.class)) {
            failHandler.onAuthenticationFailure(request, response,
                                                new LockedException("Account locked")
            );
            verify(response).setStatus(SC_UNAUTHORIZED);
            ObjectMapper mockedMapper = mapper.constructed().get(0);

            verify(mockedMapper).writeValue(eq(response.getOutputStream()), errorCaptor.capture());

            List<ErrorResponse> errors = errorCaptor.getValue();
            assertEquals(1, errors.size());

            ErrorResponse error = errors.get(0);
            assertEquals("globalError", error.fieldName());
            assertEquals("globalError", error.type());
            assertEquals("auth.blocked", error.message());
            assertEquals("validation.auth.blocked", error.errorCode());
        }
    }

    @Test
    void onAuthenticationFailure_AccountExpiredException() throws IOException {
        when(messages.getMessage(eq("validation.auth.expired"), any(), any()))
                .thenReturn("auth.expired");

        try (MockedConstruction<ObjectMapper> mapper = mockConstruction(ObjectMapper.class)) {
            failHandler.onAuthenticationFailure(request, response,
                                                new AccountExpiredException("Account expired")
            );
            verify(response).setStatus(SC_UNAUTHORIZED);
            ObjectMapper mockedMapper = mapper.constructed().get(0);

            verify(mockedMapper).writeValue(eq(response.getOutputStream()), errorCaptor.capture());

            List<ErrorResponse> errors = errorCaptor.getValue();
            assertEquals(1, errors.size());

            ErrorResponse error = errors.get(0);
            assertEquals("globalError", error.fieldName());
            assertEquals("globalError", error.type());
            assertEquals("auth.expired", error.message());
            assertEquals("validation.auth.expired", error.errorCode());
        }
    }

    @Test
    void onAuthenticationFailure_AuthenticationCredentialsNotFoundException() throws IOException {
        when(messages.getMessage(eq("validation.auth.invalidBody"), any(), any()))
                .thenReturn("auth.invalidBody");

        try (MockedConstruction<ObjectMapper> mapper = mockConstruction(ObjectMapper.class)) {
            failHandler.onAuthenticationFailure(request, response,
                                                new AuthenticationCredentialsNotFoundException(
                                                        "Credentials not found")
            );
            verify(response).setStatus(SC_UNAUTHORIZED);
            ObjectMapper mockedMapper = mapper.constructed().get(0);

            verify(mockedMapper).writeValue(eq(response.getOutputStream()), errorCaptor.capture());

            List<ErrorResponse> errors = errorCaptor.getValue();
            assertEquals(1, errors.size());

            ErrorResponse error = errors.get(0);
            assertEquals("globalError", error.fieldName());
            assertEquals("globalError", error.type());
            assertEquals("auth.invalidBody", error.message());
            assertEquals("validation.auth.invalidBody", error.errorCode());
        }
    }

    @Test
    void onAuthenticationFailure_CredentialsExpiredException() throws IOException {
        when(messages.getMessage(eq("validation.auth.credentialsExpired"), any(), any()))
                .thenReturn("auth.credentialsExpired");

        try (MockedConstruction<ObjectMapper> mapper = mockConstruction(ObjectMapper.class)) {
            failHandler.onAuthenticationFailure(request, response,
                                                new CredentialsExpiredException(
                                                        "Credentials expired")
            );
            verify(response).setStatus(SC_UNAUTHORIZED);
            ObjectMapper mockedMapper = mapper.constructed().get(0);

            verify(mockedMapper).writeValue(eq(response.getOutputStream()), errorCaptor.capture());

            List<ErrorResponse> errors = errorCaptor.getValue();
            assertEquals(1, errors.size());

            ErrorResponse error = errors.get(0);
            assertEquals("globalError", error.fieldName());
            assertEquals("globalError", error.type());
            assertEquals("auth.credentialsExpired", error.message());
            assertEquals("validation.auth.credentialsExpired", error.errorCode());
        }
    }

    @Test
    void onAuthenticationFailure_Unexpected() throws IOException {
        when(messages.getMessage(eq("validation.auth.unexpected"), any(), any()))
                .thenReturn("auth.unexpected");

        try (MockedConstruction<ObjectMapper> mapper = mockConstruction(ObjectMapper.class)) {
            failHandler.onAuthenticationFailure(request, response,
                                                new InternalAuthenticationServiceException(
                                                        "Unexpected")
            );
            verify(response).setStatus(SC_UNAUTHORIZED);
            ObjectMapper mockedMapper = mapper.constructed().get(0);

            verify(mockedMapper).writeValue(eq(response.getOutputStream()), errorCaptor.capture());

            List<ErrorResponse> errors = errorCaptor.getValue();
            assertEquals(1, errors.size());

            ErrorResponse error = errors.get(0);
            assertEquals("serverError", error.fieldName());
            assertEquals("serverError", error.type());
            assertEquals("auth.unexpected", error.message());
            assertEquals("validation.auth.unexpected", error.errorCode());
        }
    }

}
