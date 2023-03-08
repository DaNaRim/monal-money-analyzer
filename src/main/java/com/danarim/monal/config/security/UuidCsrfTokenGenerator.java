package com.danarim.monal.config.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Generates CSRF tokens using UUID.
 */
@Component
public class UuidCsrfTokenGenerator implements CsrfTokenGenerator {

    @Override
    public String generateCsrfToken() {
        return UUID.randomUUID().toString();
    }

}
