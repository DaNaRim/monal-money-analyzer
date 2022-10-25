package com.danarim.monal.config.exeptions;

import java.io.Serial;

public class AlreadyExistsException extends BadRequestException {

    @Serial
    private static final long serialVersionUID = 9141030559963679515L;

    public AlreadyExistsException(String message, String field, String messageCode, Object[] messageArgs) {
        super(message, field, messageCode, messageArgs);
    }
}
