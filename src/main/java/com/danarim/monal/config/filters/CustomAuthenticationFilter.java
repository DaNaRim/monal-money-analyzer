package com.danarim.monal.config.filters;

import com.danarim.monal.config.security.JwtUtil;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

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

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response
    ) throws AuthenticationException {

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

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult
    ) throws IOException {
        User user = (User) authResult.getPrincipal();

        Map<String, String> tokens = Map.of(
                JwtUtil.KEY_ACCESS_TOKEN, jwtUtil.generateAccessToken(user, request.getRequestURL().toString()),
                JwtUtil.KEY_REFRESH_TOKEN, jwtUtil.generateRefreshToken(user, request.getRequestURL().toString())
        );
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);
    }

    private record AuthenticationBody(
            String username,
            String password
    ) {

    }
}
