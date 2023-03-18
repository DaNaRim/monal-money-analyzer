package com.danarim.monal.util.appmessage;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Used to identify message in frontend.
 *
 * <p>All messages, except 'unresolved', should also have valid message in resource
 * bundle, because server can also send messages in some cases.
 */
public enum AppMessageCode {

    UNRESOLVED_CODE("unresolved"),

    REGISTRATION_CONFIRMATION_SUCCESS("registration.confirmation.success"),

    TOKEN_WRONG_TYPE("validation.token.wrong-type"),
    TOKEN_NOT_FOUND("validation.token.not-found"),
    TOKEN_USED("validation.token.used"),
    TOKEN_EXPIRED("validation.token.expired"),

    TOKEN_VERIFICATION_NOT_FOUND("validation.token.verification.not-found"),
    TOKEN_VERIFICATION_EXPIRED("validation.token.verification.expired"),
    TOKEN_VERIFICATION_USER_ENABLED("validation.token.verification.user-enabled");

    private final String code;

    AppMessageCode(String code) {
        this.code = code;
    }

    @JsonValue // used to serialize enum value instead of name in JSON
    public String getCode() {
        return code;
    }
}
