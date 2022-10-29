package com.danarim.monal.config.security.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.exceptions.InvalidTokenTypeException;
import com.danarim.monal.failHandler.CustomAuthFailureHandler;
import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import static com.danarim.monal.config.WebConfig.BACKEND_PREFIX;
import static com.danarim.monal.config.security.JwtUtil.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component("customAuthorizationFilter")
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

    private final CustomAuthFailureHandler failureHandler;
    private final JwtUtil jwtUtil;

    public CustomAuthorizationFilter(CustomAuthFailureHandler failureHandler, JwtUtil jwtUtil) {
        this.failureHandler = failureHandler;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        if (request.getServletPath().equals(BACKEND_PREFIX + "/login")
                || request.getServletPath().equals(BACKEND_PREFIX + "/jwtTokenRefresh")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String authorizationHeader = request.getHeader(AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
                throw new IllegalArgumentException("Authorization header is missing or invalid");
            }
            String token = authorizationHeader.substring(AUTHORIZATION_HEADER_PREFIX.length());
            processAuthorization(token);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            failureHandler.handleTokenException(e, request, response);
        }
    }

    private void processAuthorization(String token) {
        Algorithm algorithm = jwtUtil.getAlgorithm();
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT decodedJWT = verifier.verify(token);

        String username = decodedJWT.getSubject();
        String[] roles = decodedJWT.getClaim(CLAIM_AUTHORITIES).asArray(String.class);
        String tokenType = decodedJWT.getClaim(CLAIM_TOKEN_TYPE).asString();

        if (tokenType.equals(TOKEN_TYPE_REFRESH)) {
            throw new InvalidTokenTypeException("Invalid token type. provided: %s expected: %s"
                    .formatted(tokenType, TOKEN_TYPE_ACCESS));
        }
        Collection<GrantedAuthority> authorities = new HashSet<>();
        for (String role : roles) {
            authorities.add(new Role(RoleName.valueOf(role)));
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
