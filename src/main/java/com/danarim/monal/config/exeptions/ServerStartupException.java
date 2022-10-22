package com.danarim.monal.config.exeptions;

public class ServerStartupException extends RuntimeException {

    public ServerStartupException(String message, Throwable cause) {
        super(message, cause);
    }
}
