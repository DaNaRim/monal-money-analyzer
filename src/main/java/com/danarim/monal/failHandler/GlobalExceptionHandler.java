package com.danarim.monal.failHandler;

import com.danarim.monal.exceptions.AlreadyExistsException;
import com.danarim.monal.exceptions.BadRequestException;
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

import static com.danarim.monal.exceptions.GenericErrorType.GLOBAL_ERROR;
import static com.danarim.monal.exceptions.GenericErrorType.SERVER_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    @ExceptionHandler({BadRequestException.class, AlreadyExistsException.class})
    protected ResponseEntity<List<GenericErrorResponse>> handleBadRequestException(BadRequestException e,
                                                                                   WebRequest request
    ) {
        logger.debug("%s during request: %s : %s".formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        String message = messages.getMessage(e.getMessageCode(), e.getMessageArgs(), request.getLocale());
        GenericErrorResponse error = new GenericErrorResponse(GLOBAL_ERROR.getType(), e.getField(), message);

        return new ResponseEntity<>(Collections.singletonList(error), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<List<GenericErrorResponse>> handleInternalException(Exception e, WebRequest request) {

        logger.error("Internal server error during request: " + request.getContextPath(), e);

        String message = messages.getMessage("error.server.internal-error", null, request.getLocale());
        GenericErrorResponse error = new GenericErrorResponse(SERVER_ERROR.getType(), SERVER_ERROR.getType(), message);

        return new ResponseEntity<>(Collections.singletonList(error), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request
    ) {
        logger.debug("%s during request: %s : %s".formatted(e.getClass(), request.getContextPath(), e.getMessage()), e);

        ArrayList<GenericErrorResponse> mappedErrors = mapErrors(e.getBindingResult());
        return new ResponseEntity<>(mappedErrors, headers, status);
    }

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
