package com.danarim.monal.exceptions;

import java.io.Serial;

public class AuthorizationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4277809980506441648L;

    private static final Object[] DEFAULT_MESSAGE_ARGS = new Object[0];

    private final String messageCode;

    private final transient Object[] messageArgs;

    public AuthorizationException(String messageCode, Object[] messageArgs) {
        this.messageCode = messageCode;
        this.messageArgs = messageArgs == null ? DEFAULT_MESSAGE_ARGS : messageArgs.clone();
    }

    public AuthorizationException(Throwable cause, String messageCode, Object[] messageArgs) {
        super(cause);
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
