package com.danarim.monal.exceptions;

import com.danarim.monal.util.appmessage.AppMessageCode;

import java.io.Serial;

/**
 * Exception for invalid token. Used with not auth tokens.
 */
public class InvalidTokenException extends BadRequestException {

    @Serial
    private static final long serialVersionUID = -8559653946977226439L;

    private final AppMessageCode appMessageCode; // Token exceptions shows only in AppMessage

    /**
     * Exception for invalid token. Used with not auth tokens.
     *
     * @param message     message for logging and debugging. Not used in response.
     * @param messageCode used to get localized message from
     *                    {@link org.springframework.context.MessageSource}.
     * @param messageArgs arguments for messageCode. Can be null.
     */
    public InvalidTokenException(String message, AppMessageCode messageCode, Object[] messageArgs) {
        super(message, messageCode.getCode(), messageArgs);
        this.appMessageCode = messageCode;
    }

    public AppMessageCode getAppMessageCode() {
        return appMessageCode;
    }

}
