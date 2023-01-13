package com.danarim.monal.util;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used to send additional information to the client when endpoint returns different object
 * or request was sent not from front-end. For example, when user activates his account by link from email.
 *
 * @param message     Message that already internationalized
 * @param type        Type of message (info, error, warning, etc.)
 * @param page        Page where message should be displayed. If null, message will be displayed on the header for all pages
 * @param messageCode Message code to identify message in the front-end. Used to suggest additional actions to the user.
 *                    For example: "validation.token.expired" to suggest user to resend verification token.
 */
public record ApplicationMessage(
        @JsonProperty("message") String message,
        @JsonProperty("type") ApplicationMessageType type,
        @JsonProperty("page") String page,
        @JsonProperty("messageCode") String messageCode
) {

}
