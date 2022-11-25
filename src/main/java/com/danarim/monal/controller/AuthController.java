package com.danarim.monal.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.util.CookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(WebConfig.BACKEND_PREFIX)
public class AuthController {

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        response.addCookie(CookieUtil.deleteAccessTokenCookie());
        response.addCookie(CookieUtil.deleteRefreshTokenCookie());
    }
}
