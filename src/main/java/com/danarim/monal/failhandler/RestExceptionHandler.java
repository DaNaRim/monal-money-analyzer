package com.danarim.monal.failhandler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.danarim.monal.exceptions.ActionDeniedException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.user.web.validator.ValidPassword;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.validation.ConstraintViolationException;
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
@Order(1)
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

        ErrorResponse errorResponse = ErrorResponse.globalError(
                e.getMessageCode(),  e.getMessageArgs(), message
        );
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

        ErrorResponse errorResponse = ErrorResponse.fieldError(
                e.getMessageCode(), e.getMessageArgs(), e.getField(), message
        );
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
        String extendedMessage = message + ' ' + e.getMessage();

        ErrorResponse errorResponse =
                ErrorResponse.globalError("error.access.denied", null, extendedMessage);

        return new ResponseEntity<>(Collections.singletonList(errorResponse), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles custom {@link ActionDeniedException} thrown by service when user is not allowed to
     * perform action.
     *
     * @param e       exception caused by action that user is not allowed to perform.
     * @param request request where exception occurred.
     *
     * @return response with a list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(ActionDeniedException.class)
    protected ResponseEntity<List<ErrorResponse>> handleActionDeniedException(
            ActionDeniedException e,
            WebRequest request
    ) {
        rexLogger.warn(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(),
                                              e.getMessage()), e);

        ErrorResponse errorResponse =
                ErrorResponse.globalError("error.action.denied", null, e.getMessage());

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

        ErrorResponse errorResponse = ErrorResponse.globalError(e.getMessageCode(), null, message);

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
     * Handles validation errors thrown by {@link Valid} annotations.
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
     * Handles validation errors thrown by {@link Validated} annotations.
     *
     * @param e       exception caused by validation.
     * @param request request where exception occurred.
     *
     * @return response with list of {@link ErrorResponse}.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<List<ErrorResponse>> handleConstraintViolationException(
            ConstraintViolationException e,
            WebRequest request
    ) {
        rexLogger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(),
                                               e.getMessage()), e);

        List<ErrorResponse> mappedErrors = e.getConstraintViolations().stream()
                .map(violation -> ErrorResponse.fieldError(
                        violation.getMessageTemplate().replaceAll("[{}]", ""),
                        violation.getExecutableParameters(),
                        violation.getPropertyPath().toString().split("[.]")[1],
                        violation.getMessage())
                )
                .toList();

        return new ResponseEntity<>(mappedErrors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles server exceptions.
     *
     * @param e       exception caused by server.
     * @param request request where exception occurred.
     *
     * @return response with a list of {@link ErrorResponse} with one element.
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
     *
     * @param errors errors to map.
     */
    private ArrayList<ErrorResponse> mapErrors(BindingResult errors) {
        ArrayList<ErrorResponse> result = new ArrayList<>();

        errors.getFieldErrors().forEach(error -> result.add(mapFieldError(error)));
        errors.getGlobalErrors().forEach(error -> {
            Object[] preparedErrorCodes =
                    Arrays.copyOfRange(error.getArguments(), 1, error.getArguments().length);

            List<Object> args = new ArrayList<>(Arrays.stream(preparedErrorCodes).toList());

            result.add(ErrorResponse.globalError(
                    error.getDefaultMessage(),
                    args.toArray(),
                    messages.getMessage(
                            error.getDefaultMessage(),
                            args.toArray(),
                            LocaleContextHolder.getLocale()
                    )));
        });
        return result;
    }

    /**
     * Maps {@link FieldError} to {@link ErrorResponse}.
     * If error code is {@link ValidPassword} then it is mapped to specific password validation.
     * Password validation error codes get from a default message in format:
     * "[errorCode]:{code1=value1, code2=value2}"
     *
     *
     * @param error field error to map.
     *
     * @return {@link ErrorResponse} with field error.
     */
    private ErrorResponse mapFieldError(FieldError error) {
        String errorCode = error.getDefaultMessage();
        List<Object> args = new ArrayList<>();

        // Specific handling for password validation.
        if (List.of(error.getCodes()).contains(ValidPassword.class.getSimpleName())) {
            errorCode = "validation.user.password." + error.getDefaultMessage()
                    .split(":")[0]
                    .replaceAll("[\\[\\]]", "")
                    .toLowerCase();

            args.addAll(Arrays.stream(error.getDefaultMessage()
                                              .split(":")[1]
                                              .replaceAll("[{}]", "")
                                              .split(", "))
                                .map(s -> s.split("=")[1])
                                .toList());
        } else { // Default handling.
            Object[] preparedErrorCodes =
                    Arrays.copyOfRange(error.getArguments(), 1, error.getArguments().length);

            args.addAll(Arrays.stream(preparedErrorCodes).toList());
        }
        return ErrorResponse.fieldError(
                errorCode,
                args.toArray(),
                error.getField(),
                messages.getMessage(errorCode, args.toArray(), LocaleContextHolder.getLocale()));
    }

}
