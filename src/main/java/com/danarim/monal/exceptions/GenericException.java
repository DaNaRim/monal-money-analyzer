package com.danarim.monal.exceptions;

import java.io.Serial;

import static com.danarim.monal.exceptions.GenericErrorType.GLOBAL_ERROR;
import static com.danarim.monal.exceptions.GenericErrorType.SERVER_ERROR;

public class GenericException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1555464451457596737L;

    private static final Object[] DEFAULT_MESSAGE_ARGS = new Object[0];

    private final GenericErrorType errorType;
    private final String field;
    private final String messageCode;
    private final transient Object[] messageArgs;

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
