package com.danarim.monal.exceptions;

import java.io.Serial;

public class ServerStartupException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4913508637135584551L;

    public ServerStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
