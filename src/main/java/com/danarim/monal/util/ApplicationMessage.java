package com.danarim.monal.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used to send additional information to the client when endpoint returns different object
 * or request was sent not from front-end. For example, when user activates his account by link
 */
public class ApplicationMessage {

    /**
     * Message that already internationalized
     */
    private final String message;

    /**
     * Type of message (info, error, warning, etc.)
     */
    private final ApplicationMessageType type;

    /**
     * Page where message should be displayed. If null, message will be displayed on the header for all pages
     */
    private final String page;

    /**
     * Action code for front-end to suggest solution to the user if message is error
     * <br>
     * For example: "token.verification.resend" to resend verification token.
     */
    private final String expectClientActionCode;

    /**
     * @param message                message that already internationalized
     * @param type                   type of message (info, error, warning, etc.)
     * @param page                   page where message should be displayed. If null, message will be displayed on the header for all pages
     * @param expectClientActionCode action code for front-end to suggest solution to the user
     */
    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ApplicationMessage(@JsonProperty("message") String message,
                              @JsonProperty("type") ApplicationMessageType type,
                              @JsonProperty("page") String page,
                              @JsonProperty("expectClientActionCode") String expectClientActionCode
    ) {
        this.message = message;
        this.type = type;
        this.page = page;
        this.expectClientActionCode = expectClientActionCode;
    }

    public String getMessage() {
        return message;
    }

    public ApplicationMessageType getType() {
        return type;
    }

    public String getPage() {
        return page;
    }

    public String getExpectClientActionCode() {
        return expectClientActionCode;
    }
}
