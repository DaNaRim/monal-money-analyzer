package com.danarim.monal.config.security;

/**
 * Interface for generating CSRF tokens.
 */
public interface CsrfTokenGenerator {

    String generateCsrfToken();

}
