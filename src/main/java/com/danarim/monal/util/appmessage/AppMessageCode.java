package com.danarim.monal.util.appmessage;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Used to identify message in frontend.
 *
 * <p>All messages should also have valid message in resource
 * bundle, because server can also send messages in some cases.
 *
 * <p>AppMessagesCode words must be split only with underscores
 */
public enum AppMessageCode {

    ACCOUNT_CONFIRMATION_SUCCESS("account_confirmation_success"), // login page

    TOKEN_WRONG_TYPE("validation_token_wrong_type"), // login page
    TOKEN_NOT_FOUND("validation_token_not_found"), // login page
    TOKEN_USED("validation_token_used"), // login page
    TOKEN_EXPIRED("validation_token_expired"), // login page

    // Specific messages because code is used in fronted to suggest user to resend email
    TOKEN_VERIFICATION_NOT_FOUND("validation_token_verification_not_found"), // login page
    TOKEN_VERIFICATION_EXPIRED("validation_token_verification_expired"), // login page
    TOKEN_VERIFICATION_USER_ENABLED("validation_token_verification_user_enabled"); // login page

    private final String code;

    AppMessageCode(String code) {
        this.code = code;
    }

    @JsonValue // used to serialize enum value instead of name in JSON
    public String getCode() {
        return code;
    }
}
