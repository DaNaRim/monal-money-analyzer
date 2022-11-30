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

import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.danarim.monal.DbUserFiller.AUTH_JSON_USER;
import static com.danarim.monal.TestUtils.postExt;
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
    void testRefreshToken() throws Exception {

        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        Cookie refreshTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_REFRESH_TOKEN);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(accessTokenCookie)
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())

                .andExpect(cookie().exists(JwtUtil.KEY_ACCESS_TOKEN))
                .andExpect(cookie().value(JwtUtil.KEY_ACCESS_TOKEN, not(accessTokenCookie.getValue())))
                .andExpect(cookie().httpOnly(JwtUtil.KEY_ACCESS_TOKEN, true))
                .andExpect(cookie().secure(JwtUtil.KEY_ACCESS_TOKEN, true));

        //refresh token doesn't change
    }

    @Test
    void testNoToken() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testExpiredToken() throws Exception {
        User user = new User("t", "e", "s", "t", new Date(), Set.of(new Role(RoleName.ROLE_USER)));

        String csrfToken = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessToken(user, "test", csrfToken, -1L);

        Cookie accessTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testInvalidToken() throws Exception {
        Cookie invalidRefreshTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, "invalid");

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(invalidRefreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testIncorrectToken() throws Exception {
        User user = new User("t", "e", "s", "t", new Date(), Set.of(new Role(RoleName.ROLE_USER)));

        String csrfToken = UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateAccessToken(user, "test", csrfToken, -1L);
        accessToken = accessToken.substring(0, accessToken.length() - 1);

        Cookie incorrectRefreshTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(incorrectRefreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

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
    void testAccessTokenAsRefresh() throws Exception {
        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        Cookie refreshTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_REFRESH_TOKEN);

        refreshTokenCookie.setValue(accessTokenCookie.getValue());

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized());
    }

}
