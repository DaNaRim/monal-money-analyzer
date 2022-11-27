package com.danarim.monal.failHandler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.danarim.monal.exceptions.AlreadyExistsException;
import com.danarim.monal.exceptions.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.danarim.monal.exceptions.GenericErrorType.GLOBAL_ERROR;
import static com.danarim.monal.exceptions.GenericErrorType.SERVER_ERROR;

/**
 * Handles exceptions thrown by controllers.
 * <br>
 * All methods except auth handlers must return {@link ResponseEntity} with list of {@link GenericErrorResponse} as body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String LOG_TEMPLATE = "%s during request: %s : %s";

    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    /**
     * Handles validation exceptions thrown by services.
     */
    @ExceptionHandler({BadRequestException.class, AlreadyExistsException.class})
    protected ResponseEntity<List<GenericErrorResponse>> handleBadRequestException(BadRequestException e,
                                                                                   WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        String message = messages.getMessage(e.getMessageCode(), e.getMessageArgs(), request.getLocale());
        GenericErrorResponse error = new GenericErrorResponse(GLOBAL_ERROR.getType(), e.getField(), message);

        return new ResponseEntity<>(Collections.singletonList(error), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link AccessDeniedException} thrown by Spring Security when user is not authorized to access resource.
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<List<GenericErrorResponse>> handleAccessDeniedException(AccessDeniedException e,
                                                                                     WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        String message = messages.getMessage("error.access.denied", null, request.getLocale());
        GenericErrorResponse error = new GenericErrorResponse(GLOBAL_ERROR.getType(), GLOBAL_ERROR.getType(), message);

        return new ResponseEntity<>(Collections.singletonList(error), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link TokenExpiredException} thrown by auth refresh endpoint when token is expired.
     *
     * @return body with error message. Not {@link GenericErrorResponse} because it is not handled by frontend.
     */
    @ExceptionHandler(TokenExpiredException.class)
    protected ResponseEntity<String> handleTokenExpiredException(TokenExpiredException e,
                                                                 WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(messages.getMessage("validation.auth.token.expired", null, request.getLocale()));
    }

    /**
     * Handles {@link JWTVerificationException} thrown by auth refresh endpoint when token is invalid.
     *
     * @return body with error message. Not {@link GenericErrorResponse} because it is not handled by frontend.
     */
    @ExceptionHandler(JWTVerificationException.class)
    protected ResponseEntity<String> handleJWTVerificationException(JWTVerificationException e,
                                                                    WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(messages.getMessage("validation.auth.token.invalid", null, request.getLocale()));
    }

    /**
     * Handles server exceptions.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<List<GenericErrorResponse>> handleInternalException(Exception e, WebRequest request) {

        logger.error("Internal server error during request: " + request.getContextPath(), e);

        String message = messages.getMessage("error.server.internal-error", null, request.getLocale());
        GenericErrorResponse error = new GenericErrorResponse(SERVER_ERROR.getType(), SERVER_ERROR.getType(), message);

        return new ResponseEntity<>(Collections.singletonList(error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors thrown by {@link Validated} and {@link Valid} annotations.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        ArrayList<GenericErrorResponse> mappedErrors = mapErrors(e.getBindingResult());
        return new ResponseEntity<>(mappedErrors, headers, status);
    }

    /**
     * Maps errors from {@link BindingResult} to list of {@link GenericErrorResponse}.
     */
    private static ArrayList<GenericErrorResponse> mapErrors(BindingResult errors) {
        ArrayList<GenericErrorResponse> result = new ArrayList<>();

        errors.getFieldErrors().forEach(
                error -> result.add(new GenericErrorResponse(error.getCode(),
                        error.getField(),
                        error.getDefaultMessage()))
        );
        errors.getGlobalErrors().forEach(
                error -> result.add(new GenericErrorResponse(error.getCode(),
                        GLOBAL_ERROR.getType(),
                        error.getDefaultMessage()))
        );
        return result;
    }
}
