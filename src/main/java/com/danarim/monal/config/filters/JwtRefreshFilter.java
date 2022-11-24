package com.danarim.monal.config.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.exceptions.AuthorizationException;
import com.danarim.monal.user.persistence.model.User;
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
import java.util.concurrent.TimeUnit;

/**
 * Filter to refresh JWT token
 * <br>
 * Exception handles by {@link ExceptionHandlerFilter}
 */
@Component
public class JwtRefreshFilter extends OncePerRequestFilter {

    public static final String REFRESH_TOKEN_ENDPOINT = "/jwtTokenRefresh"; //TODO /auth/refresh

    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public JwtRefreshFilter(UserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Process the authentication refresh token cookie and pass to processRefresh() method create a new JWT token
     * <br>
     * If the token is valid, a new token will be created and set in cookie
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

        if (!request.getRequestURI().equals(WebConfig.BACKEND_PREFIX + REFRESH_TOKEN_ENDPOINT)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            Cookie accessTokenCookie = processRefresh(token, request.getRequestURL().toString());

            response.addCookie(accessTokenCookie);
        } catch (TokenExpiredException e) {
            throw new AuthorizationException(e, "validation.auth.token.expired", null);
        } catch (JWTVerificationException | UsernameNotFoundException e) {
            throw new AuthorizationException(e, "validation.auth.token.invalid", null);
        }
    }

    /**
     * Process the refresh token and if valid return new cookie with new access token
     *
     * @return Cookie with new access token
     */
    private Cookie processRefresh(String token, String requestURL) {
        DecodedJWT decodedJWT = jwtUtil.decode(token);

        String email = decodedJWT.getSubject();
        User user = (User) userDetailsService.loadUserByUsername(email);

        String accessToken = jwtUtil.generateAccessToken(user, requestURL);

        Cookie accessTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(JwtUtil.ACCESS_TOKEN_DEFAULT_EXPIRATION_IN_DAYS));

        return accessTokenCookie;
    }
}
