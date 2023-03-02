package com.danarim.monal.exceptions;

import com.danarim.monal.failhandler.ResponseErrorType;

import java.io.Serial;

/**
 * Used when the request processing failed because of a validation error on a specific field.
 * <br>
 * If validation error is caused by a global error, use {@link BadRequestException} instead.
 *
 * @see ResponseErrorType#FIELD_VALIDATION_ERROR
 */
public class BadFieldException extends BadRequestException {

    @Serial
    private static final long serialVersionUID = 2414712437325818911L;

    /**
     * Name of the field that caused the exception.
     */
    private final String field;

    /**
     * Exception for field validation errors.
     *
     * @param message     message for logging and debugging. Not used in response.
     * @param messageCode used to get localized message from
     *                    {@link org.springframework.context.MessageSource}.
     * @param messageArgs arguments for messageCode. Can be null.
     * @param field       name of the field that caused the exception.
     */
    public BadFieldException(String message,
                             String messageCode,
                             Object[] messageArgs,
                             String field
    ) {
        super(message, messageCode, messageArgs);
        this.field = field;
    }

    /**
     * Exception for field validation errors.
     *
     * @param message     message for logging and debugging. Not used in response.
     * @param cause       cause of the exception.
     * @param messageCode used to get localized message from
     *                    {@link org.springframework.context.MessageSource}.
     * @param messageArgs arguments for messageCode. Can be null.
     * @param field       name of the field that caused the exception.
     */
    public BadFieldException(String message,
                             Throwable cause,
                             String messageCode,
                             Object[] messageArgs,
                             String field
    ) {
        super(message, cause, messageCode, messageArgs);
        this.field = field;
    }

    public String getField() {
        return field;
    }

}
