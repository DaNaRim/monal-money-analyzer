package com.danarim.monal.config.security.filters;

import com.danarim.monal.DbUserFiller;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.config.security.auth.AuthResponseEntity;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.util.UUID;

import static com.danarim.monal.DbUserFiller.AUTH_JSON_USER;
import static com.danarim.monal.TestUtils.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbUserFiller.class)
class CustomAuthorizationFilterIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void testNoLoginAccess() throws Exception {
        mockMvc.perform(getExt("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub"))
                .andExpect(status().isForbidden());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/adminStub"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLoggedUserAccess() throws Exception {
        mockMvc.perform(getExtWithAuth(WebConfig.API_V1_PREFIX + "/stub", RoleName.ROLE_USER, mockMvc))
                .andExpect(status().isOk());

        mockMvc.perform(getExtWithAuth(WebConfig.API_V1_PREFIX + "/adminStub", RoleName.ROLE_USER, mockMvc))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLoggedAdminAccess() throws Exception {
        mockMvc.perform(getExtWithAuth(WebConfig.API_V1_PREFIX + "/stub", RoleName.ROLE_ADMIN, mockMvc))
                .andExpect(status().isOk());

        mockMvc.perform(getExtWithAuth(WebConfig.API_V1_PREFIX + "/adminStub", RoleName.ROLE_ADMIN, mockMvc))
                .andExpect(status().isOk());
    }

    @Test
    void testLoggedAccessWithoutCsrf() throws Exception {
        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                        .cookie(accessTokenCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/adminStub")
                        .cookie(accessTokenCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void testExpiredToken() throws Exception {
        String csrfToken = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessToken(DbUserFiller.testUser, csrfToken, -1L);

        Cookie accessTokenCookie = new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, accessToken);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testInvalidToken() throws Exception {
        Cookie invalidAccessTokenCookie = new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, "invalid");

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                        .cookie(invalidAccessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testIncorrectToken() throws Exception {
        String accessToken = jwtUtil.generateAccessToken(DbUserFiller.testUser, "doesn`t matter", -1L);
        accessToken = accessToken.substring(0, accessToken.length() - 1);

        Cookie incorrectAccessTokenCookie = new Cookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, accessToken);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                        .cookie(incorrectAccessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testRefreshTokenAsAccess() throws Exception {
        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        AuthResponseEntity authResponse = new ObjectMapper().readValue(json, AuthResponseEntity.class);

        String csrfToken = authResponse.csrfToken();
        Cookie accessTokenCookie = result.getResponse().getCookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY);
        Cookie refreshTokenCookie = result.getResponse().getCookie(CookieUtil.COOKIE_REFRESH_TOKEN_KEY);

        accessTokenCookie.setValue(refreshTokenCookie.getValue());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testBlockedToken() throws Exception {
        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY);

        jwtUtil.blockToken(accessTokenCookie.getValue());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/stub")
                        .cookie(accessTokenCookie))
                .andExpect(status().isForbidden());
    }
}

