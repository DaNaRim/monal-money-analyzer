package com.danarim.monal.config.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.exceptions.AuthorizationException;
import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * Validate the JWT token and set the authentication in the security context if authentication is successful
 * <br>
 * Exception handles by {@link ExceptionHandlerFilter}
 */
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public CustomAuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Validate the JWT token from request body and pass to processAuthorization() method for further processing
     *
     * @throws AuthorizationException if authorization fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        String token = Optional.ofNullable(WebUtils.getCookie(request, JwtUtil.KEY_ACCESS_TOKEN))
                .map(Cookie::getValue)
                .orElse(null);

        if (token == null
                || request.getRequestURI().equals(WebConfig.API_V1_PREFIX + JwtRefreshFilter.REFRESH_TOKEN_ENDPOINT)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            processAuthorization(token);

            filterChain.doFilter(request, response);
        } catch (TokenExpiredException e) {
            throw new AuthorizationException(e, "validation.auth.token.expired", null);
        } catch (JWTVerificationException e) {
            throw new AuthorizationException(e, "validation.auth.token.invalid", null);
        }
    }

    /**
     * Process token and set authentication in the security context
     *
     * @param token the JWT token
     */
    private void processAuthorization(String token) {
        DecodedJWT decodedJWT = jwtUtil.decode(token);

        String username = decodedJWT.getSubject();
        String[] roles = decodedJWT.getClaim(JwtUtil.CLAIM_AUTHORITIES).asArray(String.class);

        Collection<GrantedAuthority> authorities = new HashSet<>();
        for (String role : roles) {
            authorities.add(new Role(RoleName.valueOf(role)));
        }
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(username, null, authorities));
    }
}
