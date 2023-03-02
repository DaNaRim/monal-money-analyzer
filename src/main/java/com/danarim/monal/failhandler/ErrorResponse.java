package com.danarim.monal.failhandler;

/**
 * Used to return an error response to the client. Do not use constructors directly, use the static
 * methods instead.
 *
 * @param type      Type of error. See {@link ResponseErrorType}.
 * @param errorCode error code to identify the error.
 * @param fieldName The name of the field that caused the error. Use
 *                  {@link ResponseErrorType#GLOBAL_ERROR} value for global errors and
 *                  {@link ResponseErrorType#SERVER_ERROR} value for server errors. The reason for
 *                  this is that the client can use this to display the error message
 * @param message   Error message.
 */
public record ErrorResponse(
        String type,
        String errorCode,
        String fieldName,
        String message
) {

    /**
     * Creates a new instance of {@link ErrorResponse} for a field validation error.
     *
     * @param errorCode error code to identify the error.
     * @param fieldName The name of the field that caused the error.
     * @param message   already localized error message.
     *
     * @return A new instance of {@link ErrorResponse}.
     */
    public static ErrorResponse fieldError(String errorCode, String fieldName, String message) {
        return new ErrorResponse(ResponseErrorType.FIELD_VALIDATION_ERROR.getName(),
                                 errorCode,
                                 fieldName,
                                 message);
    }

    /**
     * Creates a new instance of {@link ErrorResponse} for a global error.
     *
     * @param errorCode error code to identify the error.
     * @param message   already localized error message.
     *
     * @return A new instance of {@link ErrorResponse}.
     */
    public static ErrorResponse globalError(String errorCode, String message) {
        return new ErrorResponse(ResponseErrorType.GLOBAL_ERROR.getName(),
                                 errorCode,
                                 ResponseErrorType.GLOBAL_ERROR.getName(),
                                 message);
    }

    /**
     * Creates a new instance of {@link ErrorResponse} for a server error.
     *
     * @param errorCode error code to identify the error.
     * @param message   already localized error message.
     *
     * @return A new instance of {@link ErrorResponse}.
     */
    public static ErrorResponse serverError(String errorCode, String message) {
        return new ErrorResponse(ResponseErrorType.SERVER_ERROR.getName(),
                                 errorCode,
                                 ResponseErrorType.SERVER_ERROR.getName(),
                                 message);
    }

}
