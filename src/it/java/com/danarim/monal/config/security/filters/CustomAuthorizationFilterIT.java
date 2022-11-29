package com.danarim.monal.config.security.filters;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.config.security.auth.AuthResponseEntity;
import com.danarim.monal.user.persistence.dao.RoleDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class CustomAuthorizationFilterIT {

    private static final String USER_USERNAME = "AuthorizationFilter_user";
    private static final String USER_PASSWORD = "AuthorizationFilter_user";

    private static final String ADMIN_USERNAME = "AuthorizationFilter_admin";
    private static final String ADMIN_PASSWORD = "AuthorizationFilter_admin";

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
                Set.of(userRole)
        );
        user.setEmailVerified(true);
        User admin = new User(
                "test",
                "test",
                ADMIN_USERNAME,
                adminPassword,
                Set.of(userRole, adminRole)
        );
        admin.setEmailVerified(true);
        userDao.save(user);
        userDao.save(admin);

        isDBInitialized = true;
    }

    @Test
    void testNoLoginAccess() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/stub"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/adminStub"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLoggedUserAccess() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(USER_USERNAME, USER_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        AuthResponseEntity authResponse = new ObjectMapper().readValue(json, AuthResponseEntity.class);

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        String csrfToken = authResponse.csrfToken();

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/stub")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/adminStub")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLoggedAdminAccess() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(ADMIN_USERNAME, ADMIN_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        AuthResponseEntity authResponse = new ObjectMapper().readValue(json, AuthResponseEntity.class);

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        String csrfToken = authResponse.csrfToken();

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/stub")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk());

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/adminStub")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk());
    }

    @Test
    void testLoggedAccessWithoutCsrf() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(ADMIN_USERNAME, ADMIN_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/stub")
                        .cookie(accessTokenCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/adminStub")
                        .cookie(accessTokenCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void testExpiredToken() throws Exception {
        User user = new User("t", "e", "s", "t", Set.of(new Role(RoleName.ROLE_USER)));

        String csrfToken = UUID.randomUUID().toString();
        String accessToken = jwtUtil.generateAccessToken(user, "test", csrfToken, -1L);

        Cookie accessTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/stub")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testInvalidToken() throws Exception {
        Cookie invalidAccessTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, "invalid");

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/stub")
                        .cookie(invalidAccessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testIncorrectToken() throws Exception {
        User user = new User("t", "e", "s", "t", Set.of(new Role(RoleName.ROLE_USER)));

        String accessToken = jwtUtil.generateAccessToken(user, "test", "doesn`t matter", -1L);
        accessToken = accessToken.substring(0, accessToken.length() - 1);

        Cookie incorrectAccessTokenCookie = new Cookie(JwtUtil.KEY_ACCESS_TOKEN, accessToken);

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/stub")
                        .cookie(incorrectAccessTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$").exists());
    }

    @Test
    void testRefreshTokenAsAccess() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(USER_USERNAME, USER_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        AuthResponseEntity authResponse = new ObjectMapper().readValue(json, AuthResponseEntity.class);

        String csrfToken = authResponse.csrfToken();
        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        Cookie refreshTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_REFRESH_TOKEN);

        accessTokenCookie.setValue(refreshTokenCookie.getValue());

        mockMvc.perform(get(WebConfig.API_V1_PREFIX + "/stub")
                        .header("X-CSRF-TOKEN", csrfToken)
                        .cookie(accessTokenCookie))
                .andExpect(status().isUnauthorized());
    }
}

