package com.danarim.monal.exceptions;

public enum GenericErrorType {
    FIELD_VALIDATION_ERROR("fieldValidationError"),
    GLOBAL_ERROR("globalError"),
    SERVER_ERROR("serverError");

    private final String type;

    GenericErrorType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
