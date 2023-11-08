package com.danarim.monal.failhandler;

import java.util.Arrays;
import java.util.Objects;

/**
 * Used to return an error response to the client. Do not use constructors directly, use the static
 * methods instead.
 *
 * @param type      Type of error. See {@link ResponseErrorType}.
 * @param errorCode error code to identify the error. Use
 * @param errorArgs arguments for the error code.
 * @param fieldName The name of the field that caused the error. Use
 *                  {@link ResponseErrorType#GLOBAL_ERROR} value for global errors and
 *                  {@link ResponseErrorType#SERVER_ERROR} value for server errors. The reason for
 *                  this is that the client can use this to display the error message
 * @param message   Error message.
 */
public record ErrorResponse(
        String type,
        String errorCode,
        Object[] errorArgs,
        String fieldName,
        String message
) {

    /**
     * Creates a new instance of {@link ErrorResponse} for a field validation error.
     *
     * @param errorCode error code to identify the error.
     * @param errorArgs arguments for the error code.
     * @param fieldName The name of the field that caused the error.
     * @param message   already localized error message.
     *
     * @return A new instance of {@link ErrorResponse}.
     */
    public static ErrorResponse fieldError(String errorCode,
                                           Object[] errorArgs,
                                           String fieldName,
                                           String message
    ) {
        return new ErrorResponse(ResponseErrorType.FIELD_VALIDATION_ERROR.getName(),
                                 errorCode,
                                 errorArgs,
                                 fieldName,
                                 message);
    }

    /**
     * Creates a new instance of {@link ErrorResponse} for a global error.
     *
     * @param errorCode error code to identify the error.
     * @param errorArgs arguments for the error code.
     * @param message   already localized error message.
     *
     * @return A new instance of {@link ErrorResponse}.
     */
    public static ErrorResponse globalError(String errorCode, Object[] errorArgs, String message) {
        return new ErrorResponse(ResponseErrorType.GLOBAL_ERROR.getName(),
                                 errorCode,
                                 errorArgs,
                                 ResponseErrorType.GLOBAL_ERROR.getName(),
                                 message);
    }

    /**
     * Creates a new instance of {@link ErrorResponse} for a server error.
     * This type of error does not use arguments.
     *
     * @param errorCode error code to identify the error.
     * @param message   already localized error message.
     *
     * @return A new instance of {@link ErrorResponse}.
     */
    public static ErrorResponse serverError(String errorCode, String message) {
        return new ErrorResponse(ResponseErrorType.SERVER_ERROR.getName(),
                                 errorCode,
                                 null,
                                 ResponseErrorType.SERVER_ERROR.getName(),
                                 message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ErrorResponse that = (ErrorResponse) o;
        return Objects.equals(type, that.type) && Objects.equals(errorCode,
                                                                 that.errorCode)
                && Arrays.equals(errorArgs, that.errorArgs) && Objects.equals(
                fieldName,
                that.fieldName) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, errorCode, fieldName, message);
        result = 31 * result + Arrays.hashCode(errorArgs);
        return result;
    }

    @Override
    public String toString() {
        return "ErrorResponse{"
                + "type='" + type + '\''
                + ", errorCode='" + errorCode + '\''
                + ", errorArgs=" + Arrays.toString(errorArgs)
                + ", fieldName='" + fieldName + '\''
                + ", message='" + message + '\''
                + '}';
    }

}
