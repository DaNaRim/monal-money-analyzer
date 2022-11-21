package com.danarim.monal.failHandler;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.danarim.monal.exceptions.BadRequestException;
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

import static com.danarim.monal.exceptions.GenericErrorType.GLOBAL_ERROR;
import static com.danarim.monal.exceptions.GenericErrorType.SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component("CustomAuthenticationFailureHandler")
public class CustomAuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final MessageSource messages;
    private final LocaleResolver localeResolver;

    public CustomAuthFailureHandler(MessageSource messages, LocaleResolver localeResolver) {
        this.messages = messages;
        this.localeResolver = localeResolver;
    }

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

    public void handleTokenException(Exception e,
                                     HttpServletRequest request,
                                     HttpServletResponse response
    ) throws IOException {
        Locale locale = localeResolver.resolveLocale(request);

        String message;
        String type = GLOBAL_ERROR.getType();

        if (e instanceof BadRequestException badReqEx) {
            message = messages.getMessage(badReqEx.getMessageCode(), badReqEx.getMessageArgs(), locale);
        } else if (e instanceof IllegalArgumentException) {
            message = messages.getMessage("validation.auth.token.invalid.prefix", null, locale);
        } else if (e instanceof TokenExpiredException) {
            message = messages.getMessage("validation.auth.token.expired", null, locale);
        } else if (e instanceof JWTDecodeException) {
            message = messages.getMessage("validation.auth.token.incorrect", null, locale);
        } else if (e instanceof JWTVerificationException || e instanceof NullPointerException) {
            message = messages.getMessage("validation.auth.token.invalid", null, locale);
        } else {
            logger.error("Error processing token", e);
            type = SERVER_ERROR.getType();
            message = messages.getMessage("error.server.internal-error", null, locale);
        }
        GenericErrorResponse genericErrorResponse = new GenericErrorResponse(
                type,
                type,
                message
        );
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(UNAUTHORIZED.value());
        new ObjectMapper().writeValue(response.getOutputStream(), genericErrorResponse);
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
