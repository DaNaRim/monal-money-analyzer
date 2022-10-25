package com.danarim.monal.config.exeptions;

import java.io.Serial;

public class InternalServerException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2239953445948010676L;

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }

}
