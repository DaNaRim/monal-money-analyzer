package com.danarim.monal.config.filters;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.exceptions.AuthorizationException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.exceptions.GenericErrorType;
import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Validate the JWT token and set the authentication in the security context if authentication is successful
 * <br>
 * Exception handles by {@link ExceptionHandlerFilter}
 */
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

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

        String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader == null
                || request.getRequestURI().equals(WebConfig.BACKEND_PREFIX + JwtRefreshFilter.REFRESH_TOKEN_ENDPOINT)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!authorizationHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
            throw new AuthorizationException("validation.auth.token.invalid.prefix", null);
        }
        try {
            String token = authorizationHeader.substring(AUTHORIZATION_HEADER_PREFIX.length());
            processAuthorization(token);

            filterChain.doFilter(request, response);
        } catch (BadRequestException e) {
            throw new AuthorizationException(e, e.getMessageCode(), e.getMessageArgs());
        } catch (TokenExpiredException e) {
            throw new AuthorizationException(e, "validation.auth.token.expired", null);
        } catch (JWTDecodeException | UsernameNotFoundException e) {
            throw new AuthorizationException(e, "validation.auth.token.incorrect", null);
        } catch (JWTVerificationException e) {
            throw new AuthorizationException(e, "validation.auth.token.invalid", null);
        }
    }

    /**
     * Process token and set authentication in the security context
     *
     * @param token the JWT token
     * @throws BadRequestException if provided token with not ACCESS type
     */
    private void processAuthorization(String token) {
        DecodedJWT decodedJWT = jwtUtil.decode(token);

        String username = decodedJWT.getSubject();
        String[] roles = decodedJWT.getClaim(JwtUtil.CLAIM_AUTHORITIES).asArray(String.class);
        String tokenType = decodedJWT.getClaim(JwtUtil.CLAIM_TOKEN_TYPE).asString();

        if (!tokenType.equals(JwtUtil.TOKEN_TYPE_ACCESS)) {
            throw new BadRequestException(
                    "Invalid token type. provided: %s expected: %s".formatted(tokenType, JwtUtil.TOKEN_TYPE_ACCESS),
                    GenericErrorType.GLOBAL_ERROR,
                    null,
                    "validation.auth.token.incorrectType",
                    new Object[]{tokenType, JwtUtil.TOKEN_TYPE_ACCESS}
            );
        }
        Collection<GrantedAuthority> authorities = new HashSet<>();
        for (String role : roles) {
            authorities.add(new Role(RoleName.valueOf(role)));
        }
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(username, null, authorities));
    }
}
