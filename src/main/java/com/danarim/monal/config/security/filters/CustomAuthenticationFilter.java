package com.danarim.monal.config.security.filters;

import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.failHandler.CustomAuthFailureHandler;
import com.danarim.monal.user.persistence.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Component("customAuthenticationFilter")
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final CustomAuthFailureHandler authenticationFailureHandler;
    private final JwtUtil jwtUtil;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager,
                                      CustomAuthFailureHandler authFailureHandler,
                                      JwtUtil jwtUtil
    ) {
        super.setAuthenticationManager(authenticationManager);
        this.authenticationManager = authenticationManager;
        this.authenticationFailureHandler = authFailureHandler;
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
            //handle ex and return response
            authenticationFailureHandler.handleInvalidAuthenticationBody(request, response);
            return null; //stub //TODO throw exception
        }
        final String username = authBody.username;
        final String password = authBody.password;

        UsernamePasswordAuthenticationToken token
                = new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(token);
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
