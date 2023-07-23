package com.danarim.monal.config.security.auth;

import com.danarim.monal.exceptions.InternalServerException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for authentication.
 */
public final class AuthUtil {

    private AuthUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated.");
    }

    /**
     * Gets the id of the currently logged user from the security context.
     *
     * @return id of the currently logged user
     *
     * @throws InternalServerException if user id wrongly configured in security context. This
     *                                 should never happen.
     */
    public static long getLoggedUserId() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (!(principal instanceof String)) {
            throw new InternalServerException("Principal is not a string: " + principal);
        }
        try {
            return Long.parseLong(principal.toString());
        } catch (NumberFormatException e) {
            throw new InternalServerException("User id is not a number: " + principal, e);
        }
    }

}
