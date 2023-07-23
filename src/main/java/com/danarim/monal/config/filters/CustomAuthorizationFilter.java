package com.danarim.monal.config.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.jwt.JwtUtil;
import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.util.CookieUtil;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.danarim.monal.config.security.SecurityConfig.PERMIT_ALL_API_ENDPOINTS;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * Validate the JWT token and set the authentication in the security context if authentication is
 * successful. Also check csrf tokens.
 */
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MessageSource messages;
    private final LocaleResolver localeResolver;

    /**
     * Constructor to inject dependencies.
     *
     * @param jwtUtil        util to work with jwt tokens
     * @param messages       message source to get localized messages
     * @param localeResolver locale resolver to get locale from request
     */
    public CustomAuthorizationFilter(JwtUtil jwtUtil,
                                     MessageSource messages,
                                     LocaleResolver localeResolver
    ) {
        this.jwtUtil = jwtUtil;
        this.messages = messages;
        this.localeResolver = localeResolver;
    }

    /**
     * Get the JWT token from request body and pass to processAuthorization() method for further
     * processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        String accessToken = CookieUtil.getAccessTokenValueByRequest(request);
        String csrfToken = request.getHeader("X-CSRF-TOKEN");

        if (accessToken == null
                || !request.getRequestURI().startsWith(WebConfig.API_V1_PREFIX)
                || PERMIT_ALL_API_ENDPOINTS.stream().anyMatch(request.getRequestURI()::contains)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            processAuthorization(accessToken, csrfToken);

            filterChain.doFilter(request, response);
        } catch (CsrfException e) {
            processException(request, response, SC_FORBIDDEN, "validation.auth.csrf.invalid");
        } catch (TokenExpiredException e) {
            processException(request, response, SC_UNAUTHORIZED, "validation.auth.token.expired");
        } catch (JWTVerificationException e) {
            processException(request, response, SC_UNAUTHORIZED, "validation.auth.token.invalid");
        }
    }

    /**
     * Process accessToken and set authentication in the security context.
     *
     * @param accessToken the JWT accessToken
     * @param csrfToken   csrf token from request header
     *
     * @throws CsrfException            if request send to api and csrfToken doesn't match with same
     *                                  from jwt
     * @throws JWTVerificationException if accessToken is invalid, have wrong type, expired or
     *                                  blocked
     */
    private void processAuthorization(String accessToken, String csrfToken) {
        DecodedJWT decodedJwt = jwtUtil.decode(accessToken);

        validateToken(decodedJwt, csrfToken);

        String userId = decodedJwt.getSubject();
        String[] roles = decodedJwt.getClaim(JwtUtil.CLAIM_AUTHORITIES).asArray(String.class);

        Collection<GrantedAuthority> authorities = new HashSet<>();
        for (String role : roles) {
            authorities.add(new Role(RoleName.valueOf(role)));
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId, null, authorities));
    }

    /**
     * Checks token type, csrf token and is token blocked.
     *
     * @param decodedJwt decoded JWT token
     * @param csrfToken  csrf token from request header
     *
     * @throws CsrfException            if request send to api and csrfToken doesn't match with same
     *                                  from jwt
     * @throws JWTVerificationException if accessToken have wrong type or blocked
     */
    private void validateToken(DecodedJWT decodedJwt, String csrfToken) {

        String csrfTokenFromJwt = decodedJwt.getClaim(JwtUtil.CLAIM_CSRF_TOKEN).asString();
        String tokenType = decodedJwt.getClaim(JwtUtil.CLAIM_TOKEN_TYPE).asString();
        String tokenId = decodedJwt.getId();

        if (!tokenType.equals(JwtUtil.TOKEN_TYPE_ACCESS)) {
            throw new JWTVerificationException("Invalid token type");
        }
        if (!Objects.equals(csrfToken, csrfTokenFromJwt)) {
            throw new CsrfException("Invalid csrf token");
        }
        if (jwtUtil.isTokenBlocked(Long.parseLong(tokenId))) {
            throw new JWTVerificationException("Token is blocked");
        }
    }

    /**
     * Send error message to the client.
     *
     * @param request        the request
     * @param response       the response
     * @param responseStatus status code to set in response
     * @param exMessageCode  message code to be sent to the client
     *
     * @throws IOException if writing to the response fails
     */
    private void processException(HttpServletRequest request,
                                  HttpServletResponse response,
                                  int responseStatus,
                                  String exMessageCode
    ) throws IOException {
        response.setStatus(responseStatus);
        Locale locale = localeResolver.resolveLocale(request);

        response.getWriter().write(messages.getMessage(exMessageCode, null, locale));
    }

}
