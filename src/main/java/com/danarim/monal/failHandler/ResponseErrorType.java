package com.danarim.monal.failHandler;

/**
 * Used to specify the type of error that occurred.
 */
public enum ResponseErrorType {

    /**
     * Use this when the error is caused by a field in the form.
     * <br>
     * For example, if the user entered an invalid email address or email address is already taken, use this.
     */
    FIELD_VALIDATION_ERROR("fieldValidationError"),

    /**
     * Use this when error is caused by a form but not a specific field or when the error is caused by a group of fields.
     * <br>
     * For example, when user tries to log in but his account is locked, use this type.
     */
    GLOBAL_ERROR("globalError"),

    /**
     * Use this when the error is caused by server side code.
     */
    SERVER_ERROR("serverError");

    /**
     * Used to specify the type of error in the JSON response.
     */
    private final String name;

    ResponseErrorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
