package com.danarim.monal.controller;

import com.danarim.monal.TestConfig;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.user.persistence.dao.RoleDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Import(TestConfig.class)
class AuthControllerIT {

    private static final String USER_USERNAME = "JwtRefreshFilter_user";
    private static final String USER_PASSWORD = "JwtRefreshFilter_user";

    private static final String ADMIN_USERNAME = "JwtRefreshFilter_admin";
    private static final String ADMIN_PASSWORD = "JwtRefreshFilter_admin";

    private static boolean isDBInitialized;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private JwtUtil jwtUtil;

    @PostConstruct
    public void init() {
        if (isDBInitialized) {
            return;
        }
        String adminPassword = passwordEncoder.encode(ADMIN_PASSWORD);
        String userPassword = passwordEncoder.encode(USER_PASSWORD);

        Role userRole = roleDao.findByRoleName(RoleName.ROLE_USER);
        Role adminRole = roleDao.findByRoleName(RoleName.ROLE_ADMIN);

        User user = new User(
                "test",
                "test",
                USER_USERNAME,
                userPassword,
                new Date(),
                Set.of(userRole)
        );
        user.setEmailVerified(true);
        User admin = new User(
                "test",
                "test",
                ADMIN_USERNAME,
                adminPassword,
                new Date(),
                Set.of(userRole, adminRole)
        );
        admin.setEmailVerified(true);
        userDao.save(user);
        userDao.save(admin);

        isDBInitialized = true;
    }

    @Test
    void testRefreshToken() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(USER_USERNAME, USER_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        Cookie refreshTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_REFRESH_TOKEN);

        mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(accessTokenCookie)
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(JwtUtil.KEY_ACCESS_TOKEN)) //refresh token dont update
                .andExpect(cookie().value(JwtUtil.KEY_ACCESS_TOKEN, not(accessTokenCookie.getValue())))
                .andExpect(cookie().httpOnly(JwtUtil.KEY_ACCESS_TOKEN, true));
    }

    @Test
    void testNoToken() throws Exception {
        mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testExpiredToken() throws Exception {
        User user = new User("t", "e", "s", "t", new Date(), Set.of(new Role(RoleName.ROLE_USER)));

        String csrfToken = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessToken(user, "test", csrfToken, -1L);

        Cookie accessTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);

        mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testInvalidToken() throws Exception {
        Cookie invalidRefreshTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, "invalid");

        mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/auth/refresh")
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

        mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(incorrectRefreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/logout"))
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
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(USER_USERNAME, USER_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        Cookie refreshTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_REFRESH_TOKEN);

        refreshTokenCookie.setValue(accessTokenCookie.getValue());

        mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized());
    }

}
