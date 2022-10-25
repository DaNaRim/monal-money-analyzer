package com.danarim.monal.config.exeptions;

import java.io.Serial;

public class BadRequestException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8618658914791255458L;

    private static final Object[] DEFAULT_MESSAGE_ARGS = new Object[0];

    private final String field;
    private final String messageCode;
    private final transient Object[] messageArgs;

    public BadRequestException(String message, String field, String messageCode, Object[] messageArgs) {
        super(message);
        this.field = field;
        this.messageCode = messageCode;
        this.messageArgs = messageArgs == null ? DEFAULT_MESSAGE_ARGS : messageArgs.clone();
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
