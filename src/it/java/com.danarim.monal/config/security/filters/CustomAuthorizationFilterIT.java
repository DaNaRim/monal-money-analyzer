package com.danarim.monal.config.security.filters;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.exceptions.GenericErrorType;
import com.danarim.monal.user.persistence.dao.RoleDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.Set;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
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

        mockMvc.perform(get(WebConfig.BACKEND_PREFIX + "/stub"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(WebConfig.BACKEND_PREFIX + "/adminStub"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLoggedUserAccess() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(USER_USERNAME, USER_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ObjectNode node = new ObjectMapper().readValue(json, ObjectNode.class);

        String accessToken = node.get("access_token").asText();

        mockMvc.perform(get(WebConfig.BACKEND_PREFIX + "/stub")
                        .header(AUTHORIZATION, CustomAuthorizationFilter.AUTHORIZATION_HEADER_PREFIX + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get(WebConfig.BACKEND_PREFIX + "/adminStub")
                        .header(AUTHORIZATION, CustomAuthorizationFilter.AUTHORIZATION_HEADER_PREFIX + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLoggedAdminAccess() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(ADMIN_USERNAME, ADMIN_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ObjectNode node = new ObjectMapper().readValue(json, ObjectNode.class);

        String accessToken = node.get(JwtUtil.KEY_ACCESS_TOKEN).asText();

        mockMvc.perform(get(WebConfig.BACKEND_PREFIX + "/stub")
                        .header(AUTHORIZATION, CustomAuthorizationFilter.AUTHORIZATION_HEADER_PREFIX + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get(WebConfig.BACKEND_PREFIX + "/adminStub")
                        .header(AUTHORIZATION, CustomAuthorizationFilter.AUTHORIZATION_HEADER_PREFIX + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testNoAuthPrefix() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(USER_USERNAME, USER_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ObjectNode node = new ObjectMapper().readValue(json, ObjectNode.class);

        String accessToken = node.get("access_token").asText();

        mockMvc.perform(get(WebConfig.BACKEND_PREFIX + "/stub")
                        .header(AUTHORIZATION, accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$.fieldName").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testRefreshTokenAsAccess() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(USER_USERNAME, USER_PASSWORD);

        MvcResult result = mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ObjectNode node = new ObjectMapper().readValue(json, ObjectNode.class);

        String refreshToken = node.get("refresh_token").asText();

        mockMvc.perform(get(WebConfig.BACKEND_PREFIX + "/stub")
                        .header(AUTHORIZATION, CustomAuthorizationFilter.AUTHORIZATION_HEADER_PREFIX + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$.fieldName").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$.message").exists());
    }
}

