package com.danarim.monal.exceptions;

import java.io.Serial;

public class AlreadyExistsException extends GenericException {

    @Serial
    private static final long serialVersionUID = 9141030559963679515L;

    public AlreadyExistsException(String message,
                                  GenericErrorType errorType,
                                  String field,
                                  String messageCode,
                                  Object[] messageArgs
    ) {
        super(message, errorType, field, messageCode, messageArgs);
    }
}
