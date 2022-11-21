package com.danarim.monal.exceptions;

import java.io.Serial;

/**
 * Used for service validation errors when some entity with unique field already exists.
 */
public class AlreadyExistsException extends GenericException {

    @Serial
    private static final long serialVersionUID = 9141030559963679515L;

    /**
     * Creates exception with {@link GenericErrorType} type.
     * <br>
     * If type is {@link GenericErrorType#GLOBAL_ERROR} or {@link GenericErrorType#SERVER_ERROR} then "field" is ignored
     * and can be null.
     *
     * @param message     message for logging and debugging. Not used in response.
     * @param errorType   type of error. see {@link GenericErrorType}.
     * @param field       field name that caused the exception. Use null if exception is not related to a field.
     * @param messageCode messageCode is used to get localized message from {@link org.springframework.context.MessageSource}.
     * @param messageArgs messageArgs are used to provide arguments for messageCode. Can be null.
     * @throws IllegalArgumentException if errorType is null. <br>
     *                                  Or if errorType is {@link GenericErrorType#FIELD_VALIDATION_ERROR} and field is null. <br>
     *                                  Or if messageCode is null.
     * @see GenericException
     */
    public AlreadyExistsException(String message,
                                  GenericErrorType errorType,
                                  String field,
                                  String messageCode,
                                  Object[] messageArgs
    ) {
        super(message, errorType, field, messageCode, messageArgs);
    }
}
