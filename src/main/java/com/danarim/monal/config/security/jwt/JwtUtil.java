package com.danarim.monal.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.user.persistence.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for generating, decoding and blocking JWT tokens
 */
@Component
public class JwtUtil {

    public static final long ACCESS_TOKEN_DEFAULT_EXPIRATION_IN_DAYS = 1L;
    public static final long REFRESH_TOKEN_DEFAULT_EXPIRATION_IN_DAYS = 21L;

    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String CLAIM_AUTHORITIES = "roles";
    public static final String CLAIM_CSRF_TOKEN = "xsrf";

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${secrets.jwtSecret}")
    private byte[] jwtSecret; //byte[] is used to keep the secret safe

    private Algorithm algorithm;
    private final JwtTokenDao jwtTokenDao;

    public JwtUtil(JwtTokenDao jwtTokenDao) {
        this.jwtTokenDao = jwtTokenDao;
    }

    @PostConstruct
    private void postConstruct() {
        this.algorithm = Algorithm.HMAC256(jwtSecret);
    }

    /**
     * Decode the JWT token and return the DecodedJWT object
     *
     * @param token token to be decoded
     * @see com.auth0.jwt.JWTVerifier#verify(String) possible exceptions
     */
    public DecodedJWT decode(String token) {
        return JWT.require(algorithm).build().verify(token);
    }

    /**
     * Create a new access token for the given user and save it to the database
     *
     * @param user             user to generate token for
     * @param csrfToken        csrf token to be included in the token
     * @param expirationInDays number of days after which the token will expire
     * @return generated token
     */
    public String generateAccessToken(User user, String csrfToken, long expirationInDays) {
        List<String> userRolesList = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String issuer = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        Date expirationDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expirationInDays));

        JwtTokenEntity jwtTokenEntity = jwtTokenDao.save(new JwtTokenEntity(TOKEN_TYPE_ACCESS, expirationDate, user));

        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(expirationDate)
                .withIssuer(issuer)
                .withJWTId(jwtTokenEntity.getId().toString())
                .withClaim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .withClaim(CLAIM_AUTHORITIES, userRolesList)
                .withClaim(CLAIM_CSRF_TOKEN, csrfToken)
                .sign(algorithm);
    }

    /**
     * Create a new access token for the given user and save it to the database
     * <br>
     * The token will expire in {@link #ACCESS_TOKEN_DEFAULT_EXPIRATION_IN_DAYS} days
     *
     * @param user      user to generate token for
     * @param csrfToken csrf token to be included in the token
     * @return generated token
     */
    public String generateAccessToken(User user, String csrfToken) {
        return generateAccessToken(user, csrfToken, ACCESS_TOKEN_DEFAULT_EXPIRATION_IN_DAYS);
    }

    /**
     * Create a new refresh token for the given user and save it to the database
     *
     * @param user             user to generate token for
     * @param expirationInDays number of days after which the token will expire
     * @return generated token
     */
    public String generateRefreshToken(User user, long expirationInDays) {

        String issuer = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        Date expirationDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expirationInDays));

        JwtTokenEntity jwtTokenEntity = jwtTokenDao.save(new JwtTokenEntity(TOKEN_TYPE_REFRESH, expirationDate, user));

        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(expirationDate)
                .withJWTId(jwtTokenEntity.getId().toString())
                .withIssuer(issuer)
                .withClaim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .sign(algorithm);
    }

    /**
     * Create a new refresh token for the given user and save it to the database
     * <br>
     * The token will expire in {@link #REFRESH_TOKEN_DEFAULT_EXPIRATION_IN_DAYS} days
     *
     * @param user user to generate token for
     * @return generated token
     */
    public String generateRefreshToken(User user) {
        return generateRefreshToken(user, REFRESH_TOKEN_DEFAULT_EXPIRATION_IN_DAYS);
    }

    /**
     * Block the given token. Blocked tokens cannot be used to authenticate
     *
     * @param token decoded token to block
     */
    public void blockToken(String token) {
        DecodedJWT decodedJWT = decode(token);
        long tokenId = Long.parseLong(decodedJWT.getId());
        jwtTokenDao.blockToken(tokenId);
    }

    /**
     * Check if the given token is blocked
     *
     * @param jti token id
     * @return true if the token is blocked, false otherwise
     */
    public boolean isTokenBlocked(long jti) { //use long for not to be confused with an encoded token
        return jwtTokenDao.isTokenBlocked(jti);
    }
}
