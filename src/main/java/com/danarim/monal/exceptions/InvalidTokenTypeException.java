package com.danarim.monal.exceptions;

import java.io.Serial;

public class InvalidTokenTypeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -620293111731117620L;

    public InvalidTokenTypeException(String message) {
        super(message);
    }

}
