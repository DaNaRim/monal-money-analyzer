package com.danarim.monal.config.security.filters;

import com.danarim.monal.DbUserFiller;
import com.danarim.monal.TestUtils;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.exceptions.GenericErrorType;
import com.danarim.monal.util.CookieUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static com.danarim.monal.DbUserFiller.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbUserFiller.class)
class CustomAuthenticationFilterIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLogin() throws Exception {
        mockMvc.perform(TestUtils.postExt(WebConfig.API_V1_PREFIX + "/login", AUTH_JSON_USER))
                .andExpect(status().isOk())

                .andExpect(cookie().exists(CookieUtil.COOKIE_ACCESS_TOKEN_KEY))
                .andExpect(cookie().httpOnly(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, true))
                .andExpect(cookie().secure(CookieUtil.COOKIE_ACCESS_TOKEN_KEY, true))

                .andExpect(cookie().exists(CookieUtil.COOKIE_REFRESH_TOKEN_KEY))
                .andExpect(cookie().httpOnly(CookieUtil.COOKIE_REFRESH_TOKEN_KEY, true))
                .andExpect(cookie().secure(CookieUtil.COOKIE_REFRESH_TOKEN_KEY, true));

    }

    @Test
    void testWrongLogin() throws Exception {
        String loginJson = AUTH_JSON_TEMPLATE.formatted("wrong", USER_PASSWORD);

        mockMvc.perform(TestUtils.postExt(WebConfig.API_V1_PREFIX + "/login", loginJson))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$[0].type").value(GenericErrorType.FIELD_VALIDATION_ERROR.getType()))
                .andExpect(jsonPath("$[0].fieldName").value("username"))
                .andExpect(jsonPath("$[0].message").exists());
    }

    @Test
    void testWrongPassword() throws Exception {
        String loginJson = AUTH_JSON_TEMPLATE.formatted(USER_USERNAME, "wrong");

        mockMvc.perform(TestUtils.postExt(WebConfig.API_V1_PREFIX + "/login", loginJson))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$[0].type").value(GenericErrorType.FIELD_VALIDATION_ERROR.getType()))
                .andExpect(jsonPath("$[0].fieldName").value("password"))
                .andExpect(jsonPath("$[0].message").exists());
    }

    @Test
    void testInvalidBody() throws Exception {
        String loginJson = ("{\"username2\": 123\"%s\"}").formatted(USER_USERNAME);

        mockMvc.perform(TestUtils.postExt(WebConfig.API_V1_PREFIX + "/login", loginJson))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$[0].type").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$[0].fieldName").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$[0].message").exists());
    }

    @Test
    void testNoBody() throws Exception {
        mockMvc.perform(TestUtils.postExt(WebConfig.API_V1_PREFIX + "/login", ""))
                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$[0].type").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$[0].fieldName").value(GenericErrorType.GLOBAL_ERROR.getType()))
                .andExpect(jsonPath("$[0].message").exists());
    }
}
