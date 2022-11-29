package com.danarim.monal.config.filters;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * Validate the JWT token and set the authentication in the security context if authentication is successful
 * Also check for csrf attacks
 * <br>
 * Exception handles by {@link ExceptionHandlerFilter}
 */
@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final MessageSource messages;
    private final LocaleResolver localeResolver;

    public CustomAuthorizationFilter(JwtUtil jwtUtil, MessageSource messages, LocaleResolver localeResolver) {
        this.jwtUtil = jwtUtil;
        this.messages = messages;
        this.localeResolver = localeResolver;
    }

    /**
     * Get the JWT token from request body and pass to processAuthorization() method for further processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        String accessToken = CookieUtil.getCookieValueByRequest(request, JwtUtil.KEY_ACCESS_TOKEN);
        String csrfToken = request.getHeader("X-CSRF-TOKEN");

        if (accessToken == null || request.getRequestURI().equals(WebConfig.API_V1_PREFIX + "/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            processAuthorization(accessToken, csrfToken, request.getRequestURI());

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
     * Process accessToken and set authentication in the security context
     *
     * @param accessToken the JWT accessToken
     * @param csrfToken   csrf token from request header
     * @param requestURI  request URI
     * @throws CsrfException            if request send to api and csrfToken doesn't match with same from jwt
     * @throws JWTVerificationException if accessToken is invalid, have wrong type or expired
     */
    private void processAuthorization(String accessToken, String csrfToken, String requestURI) {
        DecodedJWT decodedJWT = jwtUtil.decode(accessToken);

        String username = decodedJWT.getSubject();
        String[] roles = decodedJWT.getClaim(JwtUtil.CLAIM_AUTHORITIES).asArray(String.class);
        String csrfTokenFromJwt = decodedJWT.getClaim(JwtUtil.CLAIM_CSRF_TOKEN).asString();
        String tokenType = decodedJWT.getClaim(JwtUtil.CLAIM_TOKEN_TYPE).asString();

        if (!tokenType.equals(JwtUtil.TOKEN_TYPE_ACCESS)) {
            throw new JWTVerificationException("Invalid token type");
        }
        if (requestURI.startsWith(WebConfig.API_V1_PREFIX) && !Objects.equals(csrfToken, csrfTokenFromJwt)) {
            throw new CsrfException("Invalid csrf token");
        }

        Collection<GrantedAuthority> authorities = new HashSet<>();
        for (String role : roles) {
            authorities.add(new Role(RoleName.valueOf(role)));
        }
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(username, null, authorities));
    }

    /**
     * Set UNAUTHORIZED status code and send error message to the client
     *
     * @param request       the request
     * @param response      the response
     * @param exMessageCode message code to be sent to the client
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
