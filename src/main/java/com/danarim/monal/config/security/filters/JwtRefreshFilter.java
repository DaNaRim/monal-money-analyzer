package com.danarim.monal.config.security.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.GenericErrorType;
import com.danarim.monal.failHandler.CustomAuthFailureHandler;
import com.danarim.monal.user.persistence.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.danarim.monal.config.security.filters.CustomAuthorizationFilter.AUTHORIZATION_HEADER_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component("jwtRefreshFilter")
public class JwtRefreshFilter extends OncePerRequestFilter {

    public static final String REFRESH_TOKEN_ENDPOINT = "/jwtTokenRefresh";

    private final UserDetailsService userDetailsService;
    private final CustomAuthFailureHandler failureHandler;
    private final JwtUtil jwtUtil;

    public JwtRefreshFilter(UserDetailsService userDetailsService,
                            CustomAuthFailureHandler failureHandler,
                            JwtUtil jwtUtil
    ) {
        this.userDetailsService = userDetailsService;
        this.failureHandler = failureHandler;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        if (!request.getPathInfo().equals(WebConfig.BACKEND_PREFIX + REFRESH_TOKEN_ENDPOINT)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String authorizationHeader = request.getHeader(AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
                throw new IllegalArgumentException("Authorization header is missing or invalid");
            }
            String token = authorizationHeader.substring(AUTHORIZATION_HEADER_PREFIX.length());

            Map<String, String> tokens = processRefresh(token, request.getRequestURL().toString());

            new ObjectMapper().writeValue(response.getOutputStream(), tokens);
        } catch (Exception e) {
            failureHandler.handleTokenException(e, request, response);
        }
    }

    private Map<String, String> processRefresh(String token, String requestURL) {
        Algorithm algorithm = jwtUtil.getAlgorithm();
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT decodedJWT = verifier.verify(token);

        String email = decodedJWT.getSubject();
        String tokenType = decodedJWT.getClaim(JwtUtil.CLAIM_TOKEN_TYPE).asString();

        if (!tokenType.equals(JwtUtil.TOKEN_TYPE_REFRESH)) {
            throw new BadRequestException(
                    "Invalid token type. provided: %s expected: %s".formatted(tokenType, JwtUtil.TOKEN_TYPE_REFRESH),
                    GenericErrorType.GLOBAL_ERROR,
                    null,
                    "validation.auth.token.incorrectType",
                    new Object[]{tokenType, JwtUtil.TOKEN_TYPE_REFRESH}
            );
        }
        User user = (User) userDetailsService.loadUserByUsername(email);

        return Map.of(
                JwtUtil.KEY_ACCESS_TOKEN, jwtUtil.generateAccessToken(user, requestURL),
                JwtUtil.KEY_REFRESH_TOKEN, token
        );
    }
}
