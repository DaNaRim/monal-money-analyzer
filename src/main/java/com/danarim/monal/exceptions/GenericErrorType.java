package com.danarim.monal.exceptions;

/**
 * Used to specify the type of error that occurred.
 */
public enum GenericErrorType {
    /**
     * Use this when the error is caused by a field in the form.
     */
    FIELD_VALIDATION_ERROR("fieldValidationError"),
    /**
     * Use this when error is caused by a form but not a specific field.
     */
    GLOBAL_ERROR("globalError"),
    /**
     * Use this when the error is caused by server side code.
     */
    SERVER_ERROR("serverError");

    /**
     * Used to specify the type of error in the JSON response.
     */
    private final String type;

    GenericErrorType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
