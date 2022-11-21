package com.danarim.monal.exceptions;

import java.io.Serial;

/**
 * Exception thrown when the server fails to start. For example, missing one of required properties.
 */
public class ServerStartupException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4913508637135584551L;

    public ServerStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
