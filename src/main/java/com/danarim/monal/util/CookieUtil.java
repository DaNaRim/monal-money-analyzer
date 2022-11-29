package com.danarim.monal.util;

import com.danarim.monal.config.security.JwtUtil;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public final class CookieUtil {

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

}
