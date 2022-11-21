package com.danarim.monal.failHandler;

/**
 * Used to return an error response to the client.
 *
 * @param type      Type of error. See {@link com.danarim.monal.exceptions.GenericErrorType}.
 * @param fieldName The name of the field that caused the error or can be one of {@link com.danarim.monal.exceptions.GenericErrorType} for global errors.
 * @param message   Error message.
 */
public record GenericErrorResponse(
        String type,
        String fieldName,
        String message
) {

}
