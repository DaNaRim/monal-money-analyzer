package com.danarim.monal.failHandler;

import com.danarim.monal.exceptions.GenericErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final MessageSource messages;
    private final LocaleResolver localeResolver;

    public CustomAuthFailureHandler(MessageSource messages, LocaleResolver localeResolver) {
        this.messages = messages;
        this.localeResolver = localeResolver;
    }

    /**
     * Returns a {@link GenericErrorType} single object to the client.
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception
    ) throws IOException {
        Locale locale = localeResolver.resolveLocale(request);

        AuthError authError = Arrays.stream(AuthError.values())
                .filter(e -> e.getErrorClassName().equals(exception.getClass().getSimpleName()))
                .findFirst()
                .orElse(AuthError.UNEXPECTED);

        GenericErrorResponse errorResponse = getErrorResponse(authError, locale, exception);

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(UNAUTHORIZED.value());

        new ObjectMapper().writeValue(response.getOutputStream(), List.of(errorResponse));
    }

    private GenericErrorResponse getErrorResponse(AuthError authError,
                                                  Locale locale,
                                                  AuthenticationException exception
    ) {
        String type = GenericErrorType.GLOBAL_ERROR.getType();
        String fieldName = GenericErrorType.GLOBAL_ERROR.getType();
        String resultMessage;
        switch (authError) {
            case USERNAME_NOT_FOUND_EXCEPTION -> {
                type = GenericErrorType.FIELD_VALIDATION_ERROR.getType();
                fieldName = "username";
                resultMessage = messages.getMessage("validation.auth.notFound", null, locale);
            }
            case BAD_CREDENTIALS_EXCEPTION -> {
                type = GenericErrorType.FIELD_VALIDATION_ERROR.getType();
                fieldName = "password";
                resultMessage = messages.getMessage("validation.auth.badCredentials", null, locale);
            }
            case DISABLED_EXCEPTION -> resultMessage = messages.getMessage("validation.auth.disabled", null, locale);
            case ACCOUNT_LOCKED_EXCEPTION ->
                    resultMessage = messages.getMessage("validation.auth.blocked", null, locale);
            case ACCOUNT_EXPIRED_EXCEPTION ->
                    resultMessage = messages.getMessage("validation.auth.expired", null, locale);
            case CREDENTIALS_NOT_FOUND_EXCEPTION ->
                    resultMessage = messages.getMessage("validation.auth.invalidBody", null, locale);
            default -> {
                logger.error("Unexpected authentication error " + exception.getMessage(), exception);

                type = GenericErrorType.SERVER_ERROR.getType();
                fieldName = GenericErrorType.SERVER_ERROR.getType();
                resultMessage = messages.getMessage("validation.auth.unexpected", null, locale);
            }
        }
        return new GenericErrorResponse(type, fieldName, resultMessage);
    }

    /**
     * Enum that represents the possible authentication errors.
     * <br>
     * Used for simplify the error handling.
     */
    private enum AuthError {
        USERNAME_NOT_FOUND_EXCEPTION(UsernameNotFoundException.class.getSimpleName()),
        BAD_CREDENTIALS_EXCEPTION(BadCredentialsException.class.getSimpleName()),
        DISABLED_EXCEPTION(DisabledException.class.getSimpleName()),
        ACCOUNT_LOCKED_EXCEPTION(LockedException.class.getSimpleName()),
        ACCOUNT_EXPIRED_EXCEPTION(AccountExpiredException.class.getSimpleName()),
        CREDENTIALS_NOT_FOUND_EXCEPTION(AuthenticationCredentialsNotFoundException.class.getSimpleName()),
        UNEXPECTED("UNEXPECTED");

        private final String errorClassName;

        AuthError(String errorClassName) {
            this.errorClassName = errorClassName;
        }

        public String getErrorClassName() {
            return errorClassName;
        }
    }
}
