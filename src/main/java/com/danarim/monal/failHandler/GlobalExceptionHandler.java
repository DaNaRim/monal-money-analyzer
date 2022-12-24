package com.danarim.monal.failHandler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.util.ApplicationMessage;
import com.danarim.monal.util.ApplicationMessageType;
import com.danarim.monal.util.CookieUtil;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles exceptions thrown by controllers.
 * <br>
 * All methods except auth handlers must return {@link ResponseEntity} with list of {@link ErrorResponse} as body.
 * <br>
 * The reason for returning list instead of single object is because frontend always expects list of errors for validation.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String LOG_TEMPLATE = "%s during request: %s : %s";

    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    /**
     * Handles validation exceptions with global type.
     *
     * @param e       exception caused by global validation error.
     * @param request request where exception occurred.
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<List<ErrorResponse>> handleBadRequestException(BadRequestException e,
                                                                            WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        String message = messages.getMessage(e.getMessageCode(), e.getMessageArgs(), request.getLocale());

        ErrorResponse errorResponse = ErrorResponse.globalError(e.getMessageCode(), message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation exceptions that are caused by specific fields.
     *
     * @param e       exception caused by field validation.
     * @param request request where exception occurred.
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(BadFieldException.class)
    protected ResponseEntity<List<ErrorResponse>> handleBadFieldException(BadFieldException e,
                                                                          WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        String message = messages.getMessage(e.getMessageCode(), e.getMessageArgs(), request.getLocale());

        ErrorResponse errorResponse = ErrorResponse.fieldError(e.getMessageCode(), e.getField(), message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse), HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles {@link AccessDeniedException} thrown by Spring Security when user is not authorized to access resource.
     *
     * @param e       exception caused access denied.
     * @param request request where exception occurred.
     */
    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<List<ErrorResponse>> handleAccessDeniedException(AccessDeniedException e,
                                                                              WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        String message = messages.getMessage("error.access.denied", null, request.getLocale());

        ErrorResponse errorResponse = ErrorResponse.globalError("error.access.denied", message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link TokenExpiredException} thrown by auth refresh endpoint when token is expired.
     *
     * @param e       exception caused by expired token.
     * @param request request where exception occurred.
     * @return body with error message. Not {@link ErrorResponse} because it is not handled by frontend.
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
     * @param e       exception caused by invalid jwt token.
     * @param request request where exception occurred.
     * @return body with error message. Not {@link ErrorResponse} because it is not handled by frontend.
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
     * Handles {@link InvalidTokenException} thrown by endpoints that process account activation and password reset.
     *
     * @param e        exception caused by invalid token.
     * @param request  request where exception occurred.
     * @param response http response.
     * @return redirect to login page with error message in cookie
     */
    @ExceptionHandler(InvalidTokenException.class)
    protected View handleInvalidTokenException(InvalidTokenException e,
                                               WebRequest request,
                                               HttpServletResponse response
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        ApplicationMessage applicationMessage = new ApplicationMessage(
                messages.getMessage(e.getMessageCode(), e.getMessageArgs(), request.getLocale()),
                ApplicationMessageType.ERROR,
                "login",
                e.getExpectClientActionCode()
        );
        response.addCookie(CookieUtil.createAppMessageCookie(applicationMessage));

        return new RedirectView("/login");
    }

    /**
     * Handles {@link MailException} thrown by mail service.
     *
     * @param e       exception caused by mail service.
     * @param request request where exception occurred.
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(MailException.class)
    protected ResponseEntity<List<ErrorResponse>> handleMailException(MailException e,
                                                                      WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        String message = messages.getMessage("error.mail.send", null, request.getLocale());

        ErrorResponse errorResponse = ErrorResponse.serverError("error.mail.send", message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles server exceptions.
     *
     * @param e       exception caused by server.
     * @param request request where exception occurred.
     * @return response with list of {@link ErrorResponse} with one element.
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<List<ErrorResponse>> handleInternalException(Exception e, WebRequest request) {

        logger.error("Internal server error during request: " + request.getContextPath(), e);

        String message = messages.getMessage("error.server.internal-error", null, request.getLocale());

        ErrorResponse errorResponse = ErrorResponse.serverError("error.server.internal-error", message);

        return new ResponseEntity<>(Collections.singletonList(errorResponse), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors thrown by {@link Validated} and {@link Valid} annotations.
     *
     * @param e       exception caused by validation.
     * @param headers http headers.
     * @param status  http status.
     * @param request request where exception occurred.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        ArrayList<ErrorResponse> mappedErrors = mapErrors(e.getBindingResult());
        return new ResponseEntity<>(mappedErrors, headers, status);
    }

    /**
     * Maps errors from {@link BindingResult} to list of {@link ErrorResponse}.
     */
    private static ArrayList<ErrorResponse> mapErrors(BindingResult errors) {
        ArrayList<ErrorResponse> result = new ArrayList<>();

        errors.getFieldErrors().forEach(
                error -> result.add(ErrorResponse.fieldError(
                        error.getCode(),
                        error.getField(),
                        error.getDefaultMessage()
                ))
        );
        errors.getGlobalErrors().forEach(
                error -> result.add(ErrorResponse.globalError(
                        error.getCode(),
                        error.getDefaultMessage()
                ))
        );
        return result;
    }
}
