package com.danarim.monal.util.appmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Used to send additional information to the client when endpoint returns different object or
 * request was sent not from front-end. For example, when user activates his account by link from
 * email.
 *
 * @param type        Type of message (info, error, warning, etc.)
 * @param page        Page where message should be displayed.
 * @param messageCode Message code to identify message in the front-end. Used to get localized
 *                    message in frontend and suggest additional actions to the user. For example:
 *                    "validation.token.expired" to suggest user to resend verification token. We
 *                    can`t internalize message in backend because Cookies use ANSI encoding.
 *
 * @see AppMessageCode
 * @see AppMessageType
 */
public record AppMessage(
        @JsonProperty("type") AppMessageType type,
        @JsonProperty("page") String page,
        @JsonProperty("messageCode") AppMessageCode messageCode
) {

}
