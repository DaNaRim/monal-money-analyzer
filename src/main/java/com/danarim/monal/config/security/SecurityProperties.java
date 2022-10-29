package com.danarim.monal.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("secrets")
public record SecurityProperties(
        String databaseUrl,
        String databaseUsername,
        String databasePassword,

        String mailUsername,
        String mailPassword,

        String jwtSecret
) {

}
