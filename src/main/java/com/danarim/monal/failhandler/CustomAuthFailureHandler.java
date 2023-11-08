package com.danarim.monal.failhandler;

import com.danarim.monal.exceptions.ValidationCodes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Handler for authentication failures.
 */
@Component
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final MessageSource messages;
    private final LocaleResolver localeResolver;

    public CustomAuthFailureHandler(MessageSource messages, LocaleResolver localeResolver) {
        this.messages = messages;
        this.localeResolver = localeResolver;
    }

    /**
     * Processes an authentication form submission that failed.
     * <br>
     * Returns a {@link ResponseErrorType} single object as list to the client.
     *
     * @throws IOException if fails to write to the response
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

        ErrorResponse errorResponse = getErrorResponse(authError, locale, exception);

        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(UNAUTHORIZED.value());

        new ObjectMapper().writeValue(response.getOutputStream(), List.of(errorResponse));
    }

    /**
     * Generates a {@link ErrorResponse} object for the given {@link AuthError}.
     *
     * @param authError authentication error enum for easier error handling
     * @param locale    locale
     * @param exception original exception
     *
     * @return error response
     */
    private ErrorResponse getErrorResponse(AuthError authError,
                                           Locale locale,
                                           AuthenticationException exception
    ) {
        ErrorResponse errorResponse;
        switch (authError) {
            case USERNAME_NOT_FOUND_EXCEPTION -> errorResponse = ErrorResponse.fieldError(
                    "validation.auth.notFound",
                    "username",
                    messages.getMessage("validation.auth.notFound", null, locale));
            case BAD_CREDENTIALS_EXCEPTION -> errorResponse = ErrorResponse.fieldError(
                    "validation.auth.badCredentials",
                    "password",
                    messages.getMessage("validation.auth.badCredentials", null, locale));
            case DISABLED_EXCEPTION -> errorResponse = ErrorResponse.globalError(
                    ValidationCodes.AUTH_DISABLED, null,
                    messages.getMessage(ValidationCodes.AUTH_DISABLED, null, locale));
            case ACCOUNT_LOCKED_EXCEPTION -> errorResponse = ErrorResponse.globalError(
                    ValidationCodes.AUTH_BLOCKED, null,
                    messages.getMessage(ValidationCodes.AUTH_BLOCKED, null, locale));
            case ACCOUNT_EXPIRED_EXCEPTION -> errorResponse = ErrorResponse.globalError(
                    ValidationCodes.AUTH_EXPIRED, null,
                    messages.getMessage(ValidationCodes.AUTH_EXPIRED, null, locale));
            case CREDENTIALS_NOT_FOUND_EXCEPTION -> errorResponse = ErrorResponse.globalError(
                    "validation.auth.invalidBody",
                    messages.getMessage("validation.auth.invalidBody", null, locale));
            case CREDENTIALS_EXPIRED_EXCEPTION -> errorResponse = ErrorResponse.globalError(
                    "validation.auth.credentialsExpired",
                    messages.getMessage("validation.auth.credentialsExpired", null, locale));
            default -> {
                logger.error("Unexpected authentication error " + exception.getMessage(),
                             exception);
                errorResponse = ErrorResponse.serverError(
                        ValidationCodes.AUTH_UNEXPECTED,
                        messages.getMessage(ValidationCodes.AUTH_UNEXPECTED, null, locale)
                );
            }
        }
        return errorResponse;
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
        CREDENTIALS_NOT_FOUND_EXCEPTION(
                AuthenticationCredentialsNotFoundException.class.getSimpleName()),
        CREDENTIALS_EXPIRED_EXCEPTION(CredentialsExpiredException.class.getSimpleName()),
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
