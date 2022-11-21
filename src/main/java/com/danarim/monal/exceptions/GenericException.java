package com.danarim.monal.exceptions;

import java.io.Serial;

import static com.danarim.monal.exceptions.GenericErrorType.GLOBAL_ERROR;
import static com.danarim.monal.exceptions.GenericErrorType.SERVER_ERROR;

/**
 * Generic exception class for exceptions that will be returned to the client.
 * <br>
 * Casts to {@link com.danarim.monal.failHandler.GenericErrorResponse} in handlers.
 * <br>
 * Use more one of the subclasses of this class.
 */
public class GenericException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1555464451457596737L;

    private static final Object[] DEFAULT_MESSAGE_ARGS = new Object[0];


    private final GenericErrorType errorType;

    /**
     * Field name that caused the exception. Can be null if exception is not related to a field.
     */
    private final String field;

    /**
     * messageCode is used to get localized message from {@link org.springframework.context.MessageSource}.
     */
    private final String messageCode;

    /**
     * messageArgs are used to provide arguments for messageCode.
     */
    private final transient Object[] messageArgs;

    /**
     * Creates exception with {@link GenericErrorType} type.
     * <br>
     * If type is {@link GenericErrorType#GLOBAL_ERROR} or {@link GenericErrorType#SERVER_ERROR} then "field" is ignored
     * and can be null.
     *
     * @param message     message for logging and debugging. Not used in response.
     * @param errorType   type of error. see {@link GenericErrorType}.
     * @param field       field name that caused the exception. Use null if exception is not related to a field.
     * @param messageCode messageCode is used to get localized message from {@link org.springframework.context.MessageSource}.
     * @param messageArgs messageArgs are used to provide arguments for messageCode. Can be null.
     * @throws IllegalArgumentException if errorType is null. <br>
     *                                  Or if errorType is {@link GenericErrorType#FIELD_VALIDATION_ERROR} and field is null. <br>
     *                                  Or if messageCode is null.
     */
    protected GenericException(String message,
                               GenericErrorType errorType,
                               String field,
                               String messageCode,
                               Object[] messageArgs
    ) {
        super(message);

        if (errorType == null) {
            throw new IllegalArgumentException("errorType must not be null");
        }
        if (errorType == GenericErrorType.FIELD_VALIDATION_ERROR && field == null) {
            throw new IllegalArgumentException("field must not be null for FIELD_VALIDATION_ERROR");
        }
        if (messageCode == null) {
            throw new IllegalArgumentException("messageCode must not be null");
        }
        switch (errorType) {
            case GLOBAL_ERROR -> this.field = GLOBAL_ERROR.getType();
            case SERVER_ERROR -> this.field = SERVER_ERROR.getType();
            default -> this.field = field;
        }
        this.errorType = errorType;
        this.messageCode = messageCode;
        this.messageArgs = messageArgs == null ? DEFAULT_MESSAGE_ARGS : messageArgs.clone();
    }

    /**
     * Creates exception with {@link GenericErrorType} type.
     * <br>
     * If type is {@link GenericErrorType#GLOBAL_ERROR} or {@link GenericErrorType#SERVER_ERROR} then "field" is ignored
     * and can be null.
     *
     * @param message     message for logging and debugging. Not used in response.
     * @param cause       cause of exception. Not used in response.
     * @param errorType   type of error. see {@link GenericErrorType}.
     * @param field       field name that caused the exception. Use null if exception is not related to a field.
     * @param messageCode messageCode is used to get localized message from {@link org.springframework.context.MessageSource}.
     * @param messageArgs messageArgs are used to provide arguments for messageCode. Can be null.
     * @throws IllegalArgumentException if errorType is null. <br>
     *                                  Or if errorType is {@link GenericErrorType#FIELD_VALIDATION_ERROR} and field is null. <br>
     *                                  Or if messageCode is null.
     */
    public GenericException(String message,
                            Throwable cause,
                            GenericErrorType errorType,
                            String field,
                            String messageCode,
                            Object[] messageArgs
    ) {
        this(message, errorType, field, messageCode, messageArgs);
        super.initCause(cause);
    }

    public GenericErrorType getErrorType() {
        return errorType;
    }

    public String getField() {
        return field;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }

}
