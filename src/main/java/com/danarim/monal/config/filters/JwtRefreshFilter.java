package com.danarim.monal.config.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.config.security.auth.AuthResponseEntity;
import com.danarim.monal.exceptions.AuthorizationException;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Filter to refresh JWT token and return auth user state to client
 * <br>
 * Exception handles by {@link ExceptionHandlerFilter}
 */
@Component
public class JwtRefreshFilter extends OncePerRequestFilter {

    public static final String REFRESH_TOKEN_ENDPOINT = "/auth/refresh";

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtRefreshFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Process the authentication refresh token cookie and pass to processRefresh() method create a new JWT token
     * <br>
     * If the token is valid, a new token will be created and set in cookie and auth response returned to client
     *
     * @throws AuthorizationException if refresh fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        String token = Optional.ofNullable(WebUtils.getCookie(request, JwtUtil.KEY_ACCESS_TOKEN))
                .map(Cookie::getValue)
                .orElse(null);

        if (!request.getRequestURI().equals(WebConfig.API_V1_PREFIX + REFRESH_TOKEN_ENDPOINT)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            processRefresh(token, request, response);
        } catch (TokenExpiredException e) {
            throw new AuthorizationException(e, "validation.auth.token.expired", null);
        } catch (JWTVerificationException | UsernameNotFoundException e) {
            throw new AuthorizationException(e, "validation.auth.token.invalid", null);
        }
    }

    /**
     * Process the refresh token
     * If refresh token valid set new cookie with new access token and return auth response to client
     */
    private void processRefresh(String token,
                                HttpServletRequest request,
                                HttpServletResponse response
    ) throws IOException {
        DecodedJWT decodedJWT = jwtUtil.decode(token);

        String email = decodedJWT.getSubject();
        User user = (User) userDetailsService.loadUserByUsername(email);

        String accessToken = jwtUtil.generateAccessToken(user, request.getRequestURL().toString());

        Cookie accessTokenCookie = CookieUtil.createAccessTokenCookie(accessToken);
        response.addCookie(accessTokenCookie);

        AuthResponseEntity authResponseEntity = AuthResponseEntity.generateAuthResponse(user);
        new ObjectMapper().writeValue(response.getWriter(), authResponseEntity);
    }
}
