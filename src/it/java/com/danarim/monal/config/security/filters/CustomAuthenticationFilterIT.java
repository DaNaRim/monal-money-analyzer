package com.danarim.monal.config.security.filters;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.exceptions.GenericErrorType;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.PostConstruct;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
class CustomAuthenticationFilterIT {

    private static final String USER_USERNAME = "AuthenticationFilter_user";
    private static final String USER_PASSWORD = "AuthenticationFilter_user";

    private static final String ADMIN_USERNAME = "AuthenticationFilter_admin";
    private static final String ADMIN_PASSWORD = "AuthenticationFilter_admin";

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
    void testLogin() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}").formatted(USER_USERNAME, USER_PASSWORD);

        mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty());
    }

    @Test
    void testWrongLogin() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}")
                .formatted(USER_USERNAME + "wrong", USER_PASSWORD);

        mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$[0].type").value(GenericErrorType.FIELD_VALIDATION_ERROR.getType()))
                .andExpect(jsonPath("$[0].fieldName").value("username"))
                .andExpect(jsonPath("$[0].message").exists());
    }

    @Test
    void testWrongPassword() throws Exception {
        String loginJson = ("{\"username\": \"%s\",\"password\": \"%s\"}")
                .formatted(USER_USERNAME, USER_PASSWORD + "wrong");

        mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$[0].type").value(GenericErrorType.FIELD_VALIDATION_ERROR.getType()))
                .andExpect(jsonPath("$[0].fieldName").value("password"))
                .andExpect(jsonPath("$[0].message").exists());
    }

    @Test
    void testInvalidBody() throws Exception {
        String loginJson = ("{\"username2\": 123\"%s\"}").formatted(USER_USERNAME);

        mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$.fieldName").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
    }

}
