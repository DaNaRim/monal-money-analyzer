package com.danarim.monal.config;

import com.danarim.monal.config.exeptions.BadRequestException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String DEFAULT_ERROR_FIELD = "serverValidation";
    private static final String DEFAULT_ERROR_TYPE = "globalError";

    private static final String DEFAULT_SERVER_ERROR_TYPE = "serverError";

    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<List<GenericError>> handleBadRequestException(BadRequestException e, WebRequest request) {

        logger.debug("%s during request: %s : %s".formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        String field = e.getField() == null ? DEFAULT_ERROR_FIELD : e.getField();

        String message = messages.getMessage(e.getMessageCode(), e.getMessageArgs(), request.getLocale());
        GenericError error = new GenericError(DEFAULT_ERROR_TYPE, field, message);

        return new ResponseEntity<>(Collections.singletonList(error), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<List<GenericError>> handleInternalException(Exception e, WebRequest request) {

        logger.error("Internal server error during request: " + request.getContextPath(), e);

        String message = messages.getMessage("error.server.internal-error", null, request.getLocale());
        GenericError error = new GenericError(DEFAULT_SERVER_ERROR_TYPE, DEFAULT_ERROR_FIELD, message);

        return new ResponseEntity<>(Collections.singletonList(error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request
    ) {
        logger.debug("%s during request: %s : %s".formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        ArrayList<GenericError> mappedErrors = mapErrors(e.getBindingResult());
        return new ResponseEntity<>(mappedErrors, headers, status);
    }

    private static ArrayList<GenericError> mapErrors(BindingResult errors) {
        ArrayList<GenericError> result = new ArrayList<>();

        errors.getFieldErrors().forEach(
                error -> result.add(new GenericError(error.getCode(), error.getField(), error.getDefaultMessage()))
        );
        errors.getGlobalErrors().forEach(
                error -> result.add(new GenericError(error.getCode(), DEFAULT_ERROR_TYPE, error.getDefaultMessage()))
        );
        return result;
    }

    private record GenericError(
            String type,
            String fieldName,
            String message
    ) {

    }
}
