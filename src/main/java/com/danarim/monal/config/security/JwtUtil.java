package com.danarim.monal.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.user.persistence.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for generating and decoding JWT tokens
 */
@Component
public class JwtUtil {

    public static final long ACCESS_TOKEN_DEFAULT_EXPIRATION_IN_DAYS = 10L;
    public static final long REFRESH_TOKEN_DEFAULT_EXPIRATION_IN_DAYS = 30L;

    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";

    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String CLAIM_AUTHORITIES = "roles";

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${secrets.jwtSecret}")
    private String jwtSecret;

    private Algorithm algorithm;

    @PostConstruct
    private void postConstruct() {
        this.algorithm = Algorithm.HMAC256(jwtSecret.getBytes());
    }

    /**
     * See {@link JWTVerifier#verify(String)} for possible exceptions
     */
    public DecodedJWT decode(String token) {
        return JWT.require(algorithm).build().verify(token);
    }

    public String generateAccessToken(User user, String issuer, long expirationInDays) {
        List<String> userRolesList = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(
                        new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expirationInDays))
                )
                .withIssuer(issuer)
                .withClaim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .withClaim(CLAIM_AUTHORITIES, userRolesList)
                .sign(algorithm);
    }

    public String generateAccessToken(User user, String issuer) {
        return generateAccessToken(user, issuer, ACCESS_TOKEN_DEFAULT_EXPIRATION_IN_DAYS);
    }

    public String generateRefreshToken(User user, String issuer, long expirationInDays) {
        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(
                        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expirationInDays))
                )
                .withIssuer(issuer)
                .withClaim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .sign(algorithm);
    }

    public String generateRefreshToken(User user, String issuer) {
        return generateRefreshToken(user, issuer, REFRESH_TOKEN_DEFAULT_EXPIRATION_IN_DAYS);
    }
}
