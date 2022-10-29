package com.danarim.monal.exceptions;

import java.io.Serial;

public class BadRequestException extends GenericException {

    @Serial
    private static final long serialVersionUID = 8618658914791255458L;

    public BadRequestException(String message,
                               GenericErrorType errorType,
                               String field,
                               String messageCode,
                               Object[] messageArgs
    ) {
        super(message, errorType, field, messageCode, messageArgs);
    }

    public BadRequestException(String message,
                               Throwable cause,
                               GenericErrorType errorType,
                               String field,
                               String messageCode,
                               Object[] messageArgs
    ) {
        super(message, cause, errorType, field, messageCode, messageArgs);
    }
}
