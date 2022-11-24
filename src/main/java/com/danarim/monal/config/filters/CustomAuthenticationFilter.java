package com.danarim.monal.config.filters;

import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.config.security.auth.CustomAuthenticationProvider;
import com.danarim.monal.failHandler.CustomAuthFailureHandler;
import com.danarim.monal.user.persistence.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Filter to authenticate user
 * <br>
 * Authentication handles by {@link CustomAuthenticationProvider}
 * <br>
 * Exception handles by {@link CustomAuthFailureHandler}
 */
@Component
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager,
                                      JwtUtil jwtUtil
    ) {
        super.setAuthenticationManager(authenticationManager);
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Process the authentication from request body.
     *
     * @throws AuthenticationCredentialsNotFoundException if the authentication body is missing or invalid.
     * @throws AuthenticationException                    if authentication failed.
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        AuthenticationBody authBody;
        try {
            authBody = new ObjectMapper().readValue(request.getInputStream(), AuthenticationBody.class);
        } catch (IOException e) {
            throw new AuthenticationCredentialsNotFoundException("invalid AuthenticationBody", e);
        }
        final String username = authBody.username;
        final String password = authBody.password;

        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    /**
     * If authentication is successful, generate a JWT tokens, set them in cookies and return AuthResponse
     *
     * @throws IOException if an error occurs while writing the response.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult
    ) throws IOException {
        User user = (User) authResult.getPrincipal();

        String accessToken = jwtUtil.generateAccessToken(user, request.getRequestURL().toString());
        String refreshToken = jwtUtil.generateRefreshToken(user, request.getRequestURL().toString());

        Cookie accessTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(JwtUtil.ACCESS_TOKEN_DEFAULT_EXPIRATION_IN_DAYS));

        Cookie refreshTokenCookie = new Cookie(JwtUtil.KEY_REFRESH_TOKEN, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(JwtUtil.REFRESH_TOKEN_DEFAULT_EXPIRATION_IN_DAYS));

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        AuthResponseEntity authResponse = generateAuthResponse(user);

        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
    }

    private static AuthResponseEntity generateAuthResponse(User user) {
        return new AuthResponseEntity(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream()
                        .map(role -> role.getName().toString())
                        .toArray(String[]::new)
        );
    }

    private record AuthenticationBody(
            String username,
            String password
    ) {

    }

    private record AuthResponseEntity(
            String username,
            String firstName,
            String lastName,
            String[] roles
    ) {

    }
}
