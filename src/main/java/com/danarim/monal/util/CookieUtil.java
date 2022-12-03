package com.danarim.monal.util;

import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.exceptions.InternalServerException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class CookieUtil {

    private static final String COOKIE_APP_MESSAGE_KEY = "serverMessage";
    private static final long COOKIE_APP_MESSAGE_EXPIRATION_IN_HOURS = 4L;

    private CookieUtil() {
    }

    public static String getCookieValueByRequest(HttpServletRequest request, String cookieName) {
        return Optional.ofNullable(WebUtils.getCookie(request, cookieName))
                .map(Cookie::getValue)
                .orElse(null);
    }

    public static Cookie createAccessTokenCookie(String accessToken) {
        Cookie cookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(JwtUtil.ACCESS_TOKEN_DEFAULT_EXPIRATION_IN_DAYS));
        return cookie;
    }

    public static Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie(JwtUtil.KEY_REFRESH_TOKEN, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) TimeUnit.DAYS.toSeconds(JwtUtil.REFRESH_TOKEN_DEFAULT_EXPIRATION_IN_DAYS));
        return cookie;
    }

    public static Cookie deleteAccessTokenCookie() {
        Cookie cookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    public static Cookie deleteRefreshTokenCookie() {
        Cookie cookie = new Cookie(JwtUtil.KEY_REFRESH_TOKEN, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        return cookie;
    }

    public static Cookie createAppMessageCookie(ApplicationMessage message) {
        try {
            String messageJson = new ObjectMapper().writeValueAsString(message);

            Cookie cookie = new Cookie(COOKIE_APP_MESSAGE_KEY, messageJson);
            cookie.setPath("/");
            cookie.setMaxAge((int) TimeUnit.HOURS.toSeconds(COOKIE_APP_MESSAGE_EXPIRATION_IN_HOURS));
            return cookie;
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Failed to create application message cookie", e);
        }
    }

}
