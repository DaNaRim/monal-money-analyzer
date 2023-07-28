package com.danarim.monal.exceptions;

import java.io.Serial;

/**
 * An exception thrown when a user tries to perform an action that is prohibited.
 */
public class ActionDeniedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6025539814016591932L;

    public ActionDeniedException(String message) {
        super(message);
    }

    public ActionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

}
