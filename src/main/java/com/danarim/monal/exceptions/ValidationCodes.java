package com.danarim.monal.exceptions;

/**
 * Validation codes for exceptions. Client application needs to handle these codes and display
 * localized messages to the user.
 *
 * <p>Codes that used in cookies located in {@link com.danarim.monal.util.appmessage.AppMessageCode}
 */
public final class ValidationCodes {

    // Occurs when the request body is missing or invalid.
    public static final String AUTH_INVALID_BODY = "validation.auth.invalid_body";
    // Occurs when a username is missing or a user does not exist.
    public static final String AUTH_NOT_FOUND = "validation.auth.not_found";
    // Occurs when a password is missing or invalid.
    public static final String AUTH_BAD_CREDENTIALS = "validation.auth.bad_credentials";
    public static final String AUTH_DISABLED = "validation.auth.disabled";
    public static final String AUTH_EXPIRED = "validation.auth.expired";
    public static final String AUTH_BLOCKED = "validation.auth.blocked";
    public static final String AUTH_CREDENTIALS_EXPIRED = "validation.auth.credentials_expired";
    public static final String AUTH_UNEXPECTED = "validation.auth.unexpected";

    public static final String AUTH_TOKEN_EXPIRED = "validation.auth.token.expired";
    public static final String AUTH_TOKEN_INVALID = "validation.auth.token.invalid";
    public static final String AUTH_CSRF_INVALID = "validation.auth.csrf.invalid";

    /*
       These errors occur when a user sets a password that does not meet the requirements.
       These codes are from passay library.
     */
    public static final String PASSWORD_ILLEGAL_WHITESPACE =
            "validation.user.password.illegal_whitespace";
    public static final String PASSWORD_INSUFFICIENT_UPPERCASE =
            "validation.user.password.insufficient_uppercase";
    public static final String PASSWORD_INSUFFICIENT_LOWERCASE =
            "validation.user.password.insufficient_lowercase";
    public static final String PASSWORD_INSUFFICIENT_DIGIT =
            "validation.user.password.insufficient_digit";
    public static final String PASSWORD_INSUFFICIENT_SPECIAL =
            "validation.user.password.insufficient_special";
    // Sends with params (min, max).
    public static final String PASSWORD_TOO_LONG = "validation.user.password.too_long";
    // Sends with params (min, max).
    public static final String PASSWORD_TOO_SHORT = "validation.user.password.too_short";

    public static final String USER_EMAIL_REQUIRED = "validation.user.email.required";
    // Sends with params (min, max). Min can be 0.
    public static final String USER_EMAIL_SIZE = "validation.user.email.size";
    public static final String USER_EMAIL_INVALID = "validation.user.email.invalid";
    public static final String USER_EMAIL_OCCUPIED = "validation.user.email.occupied";
    public static final String USER_EMAIL_NOT_FOUND = "validation.user.email.not_found";
    public static final String USER_PASSWORD_REQUIRED = "validation.user.password.required";
    public static final String USER_PASSWORD_SAME_AS_OLD = "validation.user.password.same_as_old";
    public static final String USER_NEW_PASSWORD_REQUIRED = "validation.user.new_password.required";
    public static final String USER_CONFIRM_PASSWORD_REQUIRED =
            "validation.user.confirm_password.required";
    public static final String USER_PASSWORD_MATCHING = "validation.user.password_matching";
    public static final String USER_ALREADY_VERIFIED =
            "validation.user.already_verified";

    // Sends with params (minutesToWait, secondsToWait)
    public static final String TOKEN_CREATE_DELAY = "validation.token.create_delay";

    public static final String WALLET_NAME_REQUIRED = "validation.wallet.name.required";
    // Sends with params (min, max). Min can be 0.
    public static final String WALLET_NAME_SIZE = "validation.wallet.name.size";
    public static final String WALLET_NAME_EXISTS_FOR_USER =
            "validation.wallet.name.exists_for_user";
    public static final String WALLET_CURRENCY_REQUIRED = "validation.wallet.currency.required";
    public static final String WALLET_CURRENCY_INVALID = "validation.wallet.currency.invalid";
    public static final String WALLET_BALANCE_MAX = "validation.wallet.balance.max";
    public static final String WALLET_BALANCE_MIN = "validation.wallet.balance.min";
    public static final String WALLET_NOT_FOUND = "validation.wallet.not_found";
    public static final String WALLET_DELETE_HAS_TRANSACTIONS =
            "validation.wallet.delete.has_transactions";

    public static final String CATEGORY_NOT_FOUND = "validation.category.not_found";

    public static final String TRANSACTION_DATE_REQUIRED = "validation.transaction.date.required";
    public static final String TRANSACTION_CATEGORY_ID_REQUIRED =
            "validation.transaction.category.required";
    public static final String TRANSACTION_WALLET_ID_REQUIRED =
            "validation.transaction.wallet.required";
    // Sends with params (min, max). Min can be 0.
    public static final String TRANSACTION_DESCRIPTION_SIZE =
            "validation.transaction.description.size";
    public static final String TRANSACTION_AMOUNT_POSITIVE =
            "validation.transaction.amount.positive";
    public static final String TRANSACTION_DATE_FROM_AFTER_DATE_TO =
            "validation.transaction.date_from_after_date_to";
    public static final String TRANSACTION_NOT_FOUND = "validation.transaction.not_found";
    public static final String TRANSACTION_WALLET_HAS_DIFFERENT_CURRENCY =
            "validation.transaction.wallet_has_different_currency";

    private ValidationCodes() {
        throw new AssertionError("No ValidationCodes instances for you!");
    }
}
