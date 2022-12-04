package com.danarim.monal.controller;

import com.danarim.monal.DbUserFiller;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.danarim.monal.DbUserFiller.AUTH_JSON_USER;
import static com.danarim.monal.DbUserFiller.USER_USERNAME;
import static com.danarim.monal.TestUtils.postExt;
import static com.danarim.monal.controller.AuthControllerIT.AuthControllerUtils.authCookiesDontChange;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbUserFiller.class)
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/logout"))
                .andExpect(status().isNoContent())

                .andExpect(cookie().exists(JwtUtil.KEY_ACCESS_TOKEN))
                .andExpect(cookie().httpOnly(JwtUtil.KEY_ACCESS_TOKEN, true))
                .andExpect(cookie().maxAge(JwtUtil.KEY_ACCESS_TOKEN, 0))

                .andExpect(cookie().exists(JwtUtil.KEY_REFRESH_TOKEN))
                .andExpect(cookie().httpOnly(JwtUtil.KEY_REFRESH_TOKEN, true))
                .andExpect(cookie().maxAge(JwtUtil.KEY_REFRESH_TOKEN, 0));
    }

    @Test
    void testAuthRefresh() throws Exception {

        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        Cookie refreshTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_REFRESH_TOKEN);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(accessTokenCookie)
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.username").value(USER_USERNAME))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.csrfToken").exists())

                .andExpect(cookie().exists(JwtUtil.KEY_ACCESS_TOKEN))
                .andExpect(cookie().value(JwtUtil.KEY_ACCESS_TOKEN, not(accessTokenCookie.getValue())))
                .andExpect(cookie().httpOnly(JwtUtil.KEY_ACCESS_TOKEN, true))
                .andExpect(cookie().secure(JwtUtil.KEY_ACCESS_TOKEN, true))

                //refresh token cookie doesn't change
                .andExpect(cookie().doesNotExist(JwtUtil.KEY_REFRESH_TOKEN));
    }

    @Test
    void testAuthRefreshNoToken() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh"))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthRefreshExpiredToken() throws Exception {
        User user = new User("t", "e", "s", "t", new Date(), Set.of(new Role(RoleName.ROLE_USER)));

        String csrfToken = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessToken(user, "test", csrfToken, -1L);

        Cookie accessTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthRefreshInvalidToken() throws Exception {
        Cookie invalidRefreshTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, "invalid");

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(invalidRefreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthRefreshIncorrectToken() throws Exception {
        User user = new User("t", "e", "s", "t", new Date(), Set.of(new Role(RoleName.ROLE_USER)));

        String csrfToken = UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateAccessToken(user, "test", csrfToken, -1L);
        accessToken = accessToken.substring(0, accessToken.length() - 1);

        Cookie incorrectRefreshTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(incorrectRefreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthRefreshAccessTokenAsRefresh() throws Exception {
        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        Cookie refreshTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_REFRESH_TOKEN);

        refreshTokenCookie.setValue(accessTokenCookie.getValue());

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthGetState() throws Exception {

        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.username").value(USER_USERNAME))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.csrfToken").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthGetStateNoToken() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState"))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthGetStateExpiredToken() throws Exception {
        User user = new User("t", "e", "s", "t", new Date(), Set.of(new Role(RoleName.ROLE_USER)));

        String csrfToken = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessToken(user, "test", csrfToken, -1L);

        Cookie accessTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                        .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthGetStateInvalidToken() throws Exception {
        Cookie invalidRefreshTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, "invalid");

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                        .cookie(invalidRefreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthGetStateIncorrectToken() throws Exception {
        User user = new User("t", "e", "s", "t", new Date(), Set.of(new Role(RoleName.ROLE_USER)));

        String csrfToken = UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateAccessToken(user, "test", csrfToken, -1L);
        accessToken = accessToken.substring(0, accessToken.length() - 1);

        Cookie incorrectRefreshTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                        .cookie(incorrectRefreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    @Test
    void testAuthGetStateRefreshTokenAsAccess() throws Exception {
        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        Cookie refreshTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_REFRESH_TOKEN);

        accessTokenCookie.setValue(refreshTokenCookie.getValue());

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/getState")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$").exists())

                .andExpect(authCookiesDontChange());
    }

    protected static class AuthControllerUtils {

        protected static ResultMatcher authCookiesDontChange() {
            return result -> {
                cookie().doesNotExist(JwtUtil.KEY_ACCESS_TOKEN).match(result);
                cookie().doesNotExist(JwtUtil.KEY_REFRESH_TOKEN).match(result);
            };
        }
    }
}
