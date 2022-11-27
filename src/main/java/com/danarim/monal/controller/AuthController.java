package com.danarim.monal.controller;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.config.security.auth.AuthResponseEntity;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.util.CookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(WebConfig.API_V1_PREFIX)
public class AuthController {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthController(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        response.addCookie(CookieUtil.deleteAccessTokenCookie());
        response.addCookie(CookieUtil.deleteRefreshTokenCookie());
    }

    /**
     * Process the authentication refresh token cookie, update the JWT token and return auth user state to client
     *
     * @param request  http request
     * @param response http response
     * @return auth user state
     * @throws JWTVerificationException if token is invalid, expired or user not found
     */
    @PostMapping("auth/refresh")
    public ResponseEntity<AuthResponseEntity> refresh(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = CookieUtil.getCookieValueByRequest(request, JwtUtil.KEY_REFRESH_TOKEN);
        DecodedJWT decodedJWT = jwtUtil.decode(refreshToken);

        String email = decodedJWT.getSubject();
        User user;
        try {
            user = (User) userDetailsService.loadUserByUsername(email);
        } catch (UsernameNotFoundException e) {
            throw new JWTVerificationException("User not found", e);
        }
        String accessToken = jwtUtil.generateAccessToken(user, request.getRequestURL().toString());

        Cookie accessTokenCookie = CookieUtil.createAccessTokenCookie(accessToken);
        response.addCookie(accessTokenCookie);

        return ResponseEntity.ok(AuthResponseEntity.generateAuthResponse(user));
    }
}
