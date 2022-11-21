package com.danarim.monal.exceptions;

import java.io.Serial;

/**
 * Used when authorization process fails. For example, cause by Jwt token error.
 * <br>
 * Important to return response to client.
 * <br>
 * For authentication process, use {@link org.springframework.security.core.AuthenticationException}.
 */
public class AuthorizationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4277809980506441648L;

    private static final Object[] DEFAULT_MESSAGE_ARGS = new Object[0];

    /**
     * messageCode is used to get localized message from {@link org.springframework.context.MessageSource}.
     */
    private final String messageCode;

    /**
     * messageArgs are used to provide arguments for messageCode.
     */
    private final transient Object[] messageArgs;

    /**
     * @param messageCode messageCode is used to get localized message from {@link org.springframework.context.MessageSource}.
     * @param messageArgs messageArgs are used to provide arguments for messageCode. Can be null.
     */
    public AuthorizationException(String messageCode, Object[] messageArgs) {
        this.messageCode = messageCode;
        this.messageArgs = messageArgs == null ? DEFAULT_MESSAGE_ARGS : messageArgs.clone();
    }

    /**
     * @param messageCode messageCode is used to get localized message from {@link org.springframework.context.MessageSource}.
     * @param messageArgs messageArgs are used to provide arguments for messageCode. Can be null.
     */
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
