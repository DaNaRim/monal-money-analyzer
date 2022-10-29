package com.danarim.monal.failHandler;

public record GenericErrorResponse(
        String type,
        String fieldName,
        String message
) {

}
