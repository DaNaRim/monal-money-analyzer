package com.danarim.monal.exceptions;

import java.io.Serial;

import static com.danarim.monal.exceptions.GenericException.DEFAULT_MESSAGE_ARGS;

/**
 * Exception for invalid token. Used with not auth tokens.
 */
public class InvalidTokenException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8559653946977226439L;

    /**
     * Message code is used to get localized message from {@link org.springframework.context.MessageSource}.
     */
    private final String messageCode;

    /**
     * Message args are used to provide arguments for messageCode.
     */
    private final transient Object[] messageArgs;

    /**
     * action code for front-end to suggest solution to the user
     * <br>
     * For example: "token.verification.resend" to resend verification token.
     * @see com.danarim.monal.util.ApplicationMessage
     */
    private final String expectClientActionCode;

    /**
     * @param message     message for logging and debugging. Not used in response.
     * @param messageCode message code is used to get localized message from {@link org.springframework.context.MessageSource}.
     * @param messageArgs message args are used to provide arguments for messageCode.
     */
    public InvalidTokenException(String message, String messageCode, Object[] messageArgs, String expectClientActionCode) {
        super(message);
        this.messageCode = messageCode;
        this.messageArgs = messageArgs == null ? DEFAULT_MESSAGE_ARGS : messageArgs.clone();
        this.expectClientActionCode = expectClientActionCode;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public Object[] getMessageArgs() {
        return messageArgs.clone();
    }

    public String getExpectClientActionCode() {
        return expectClientActionCode;
    }
}
