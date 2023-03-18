package com.danarim.monal.exceptions;

import java.io.Serial;

/**
 * Exception for invalid token. Used with not auth tokens.
 */
public class InvalidTokenException extends BadRequestException {

    @Serial
    private static final long serialVersionUID = -8559653946977226439L;

    /**
     * Exception for invalid token. Used with not auth tokens.
     *
     * @param message     message for logging and debugging. Not used in response.
     * @param messageCode used to get localized message from
     *                    {@link org.springframework.context.MessageSource}.
     * @param messageArgs arguments for messageCode. Can be null.
     */
    public InvalidTokenException(String message, String messageCode, Object[] messageArgs) {
        super(message, messageCode, messageArgs);
    }

    /**
     * Exception for invalid token. Used with not auth tokens.
     *
     * @param message     message for logging and debugging. Not used in response.
     * @param cause       cause of the exception.
     * @param messageCode used to identify message in frontend.
     * @param messageArgs arguments for messageCode. Can be null.
     */
    public InvalidTokenException(String message,
                                 Throwable cause,
                                 String messageCode,
                                 Object[] messageArgs
    ) {
        super(message, cause, messageCode, messageArgs);
    }

}
