package com.danarim.monal.exceptions;

import com.danarim.monal.failHandler.ResponseErrorType;

import java.io.Serial;

/**
 * Used when the request processing failed because of a validation error.
 * <br>
 * If validation error is caused by a specific field in the form, use {@link BadFieldException} instead.
 * <br>
 * This exception should use only for global errors.
 *
 * @see ResponseErrorType#GLOBAL_ERROR
 */
public class BadRequestException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2223911471207197416L;

    protected static final Object[] DEFAULT_MESSAGE_ARGS = new Object[0];

    /**
     * Used to get localized message from {@link org.springframework.context.MessageSource}.
     */
    private final String messageCode;

    /**
     * Used to provide arguments for messageCode.
     */
    private final transient Object[] messageArgs;

    /**
     * @param message     message for logging and debugging. Not used in response.
     * @param messageCode used to get localized message from {@link org.springframework.context.MessageSource}.
     * @param messageArgs arguments for messageCode. Can be null.
     */
    public BadRequestException(String message, String messageCode, Object[] messageArgs) {
        super(message);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs == null ? DEFAULT_MESSAGE_ARGS : messageArgs.clone();
    }

    /**
     * @param message     message for logging and debugging. Not used in response.
     * @param cause       cause of the exception.
     * @param messageCode used to get localized message from {@link org.springframework.context.MessageSource}.
     * @param messageArgs arguments for messageCode. Can be null.
     */
    public BadRequestException(String message, Throwable cause, String messageCode, Object[] messageArgs) {
        super(message, cause);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs == null ? DEFAULT_MESSAGE_ARGS : messageArgs.clone();
    }

    public String getMessageCode() {
        return messageCode;
    }

    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }
}
