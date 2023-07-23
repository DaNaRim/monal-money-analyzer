package com.danarim.monal.failhandler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.InvalidTokenException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.validation.Valid;

/**
 * Handles exceptions thrown by rest controllers.
 * <br>
 * All methods except auth handlers must return {@link ResponseEntity} with list of
 * {@link ErrorResponse} as body.
 * <br>
 * The reason for returning list instead of single object is because frontend always expects list of
 * errors for validation.
 */
@RestControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    protected static final String LOG_TEMPLATE = "%s during request: %s : %s";

    private static final String INTERNAL_SERVER_ERROR_CODE = "error.server.internal-error";

    //Use own logger because Spring's logger defaults to INFO level and configures by own property.
    private static final Log rexLogger = LogFactory.getLog(RestExceptionHandler.class);

    private final MessageSource messages;

    public RestExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    /**
     * Handles validation exceptions with global type.
     *
     * @param e       exception caused by global validation error.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<List<ErrorResponse>> handleBadRequestException(BadRequestException e,
                                                                            WebRequest request
    ) {
        rexLogger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(),
                                               e.getMessage()), e);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messages.getMessage(e.getMessageCode(), e.getMessageArgs(), locale);

        ErrorResponse errorResponse = ErrorResponse.globalError(e.getMessageCode(), message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse),
                                    HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation exceptions that are caused by specific fields.
     *
     * @param e       exception caused by field validation.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(BadFieldException.class)
    protected ResponseEntity<List<ErrorResponse>> handleBadFieldException(BadFieldException e,
                                                                          WebRequest request
    ) {
        rexLogger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(),
                                               e.getMessage()), e);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messages.getMessage(e.getMessageCode(), e.getMessageArgs(), locale);

        ErrorResponse errorResponse =
                ErrorResponse.fieldError(e.getMessageCode(), e.getField(), message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse),
                                    HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles {@link AccessDeniedException} thrown by Spring Security when user is not authorized
     * to access resource.
     *
     * @param e       exception caused access denied.
     * @param request request where exception occurred.
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<List<ErrorResponse>> handleAccessDeniedException(
            AccessDeniedException e,
            WebRequest request
    ) {
        rexLogger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(),
                                               e.getMessage()), e);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messages.getMessage("error.access.denied", null, locale);

        ErrorResponse errorResponse = ErrorResponse.globalError("error.access.denied", message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link TokenExpiredException} thrown by auth refresh endpoint when token is expired.
     * Needed to return 401 instead of 403.
     *
     * @param e       exception caused by expired token.
     * @param request request where exception occurred.
     *
     * @return body with error message. Not {@link ErrorResponse} because it is not handled by
     *         frontend.
     */
    @ExceptionHandler(TokenExpiredException.class)
    protected ResponseEntity<String> handleTokenExpiredException(TokenExpiredException e,
                                                                 WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e
                .getMessage()), e);

        Locale locale = LocaleContextHolder.getLocale();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(messages.getMessage("validation.auth.token.expired", null, locale));
    }

    /**
     * Handles {@link JWTVerificationException} thrown by auth refresh endpoint when token is
     * invalid.
     *
     * @param e       exception caused by invalid jwt token.
     * @param request request where exception occurred.
     *
     * @return body with error message. Not {@link ErrorResponse} because it is not handled by
     *         frontend.
     */
    @ExceptionHandler(JWTVerificationException.class)
    protected ResponseEntity<String> handleJwtVerificationException(JWTVerificationException e,
                                                                    WebRequest request
    ) {
        rexLogger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(),
                                               e.getMessage()), e);

        Locale locale = LocaleContextHolder.getLocale();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(messages.getMessage("validation.auth.token.invalid", null, locale));
    }

    /**
     * Handles {@link InvalidTokenException} thrown by endpoints that process account activation and
     * password reset.
     *
     * @param e       exception caused by invalid token.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(InvalidTokenException.class)
    protected ResponseEntity<List<ErrorResponse>> handleInvalidTokenException(
            InvalidTokenException e,
            WebRequest request
    ) {
        rexLogger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(),
                                               e.getMessage()), e);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messages.getMessage(e.getMessageCode(), e.getMessageArgs(), locale);

        ErrorResponse errorResponse = ErrorResponse.globalError(e.getMessageCode(), message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse),
                                    HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link MailException} thrown by mail service.
     *
     * @param e       exception caused by mail service.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(MailException.class)
    protected ResponseEntity<List<ErrorResponse>> handleMailException(MailException e,
                                                                      WebRequest request
    ) {
        rexLogger.error(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(),
                                               e.getMessage()), e);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messages.getMessage("error.mail.send", null, locale);

        ErrorResponse errorResponse = ErrorResponse.serverError("error.mail.send", message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse),
                                    HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles {@link HttpMessageNotWritableException}. Needed to be overridden because default
     * implementation returns html page with exception message.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status. Doesn't matter because it is overridden to 500
     * @param request the current request
     *
     * @return list of {@link ErrorResponse} with one element.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request
    ) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = messages.getMessage(INTERNAL_SERVER_ERROR_CODE, null, locale);

        ErrorResponse errorResponse =
                ErrorResponse.serverError(INTERNAL_SERVER_ERROR_CODE, message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse),
                                    HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors thrown by {@link Validated} and {@link Valid} annotations.
     *
     * @param e       exception caused by validation.
     * @param headers http headers.
     * @param status  http status.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse}.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request
    ) {
        rexLogger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(),
                                               e.getMessage()), e);

        ArrayList<ErrorResponse> mappedErrors = mapErrors(e.getBindingResult());
        return new ResponseEntity<>(mappedErrors, headers, status);
    }

    /**
     * Handles server exceptions.
     *
     * @param e       exception caused by server.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<List<ErrorResponse>> handleInternalException(Exception e,
                                                                          WebRequest request
    ) {
        rexLogger.error("Internal server error during request: " + request.getContextPath(), e);

        Locale locale = LocaleContextHolder.getLocale();
        String message = messages.getMessage(INTERNAL_SERVER_ERROR_CODE, null, locale);

        ErrorResponse errorResponse =
                ErrorResponse.serverError(INTERNAL_SERVER_ERROR_CODE, message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse),
                                    HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Maps errors from {@link BindingResult} to list of {@link ErrorResponse}.
     */
    private static ArrayList<ErrorResponse> mapErrors(BindingResult errors) {
        ArrayList<ErrorResponse> result = new ArrayList<>();

        errors.getFieldErrors()
                .forEach(error -> result.add(ErrorResponse.fieldError(error.getCode(),
                                                                      error.getField(),
                                                                      error.getDefaultMessage())));
        errors.getGlobalErrors()
                .forEach(error -> result.add(ErrorResponse.globalError(error.getCode(),
                                                                       error.getDefaultMessage())));
        return result;
    }

}
