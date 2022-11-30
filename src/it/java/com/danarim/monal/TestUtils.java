package com.danarim.monal;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import com.danarim.monal.config.security.auth.AuthResponseEntity;
import com.danarim.monal.user.persistence.model.RoleName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.Cookie;

import static com.danarim.monal.DbUserFiller.AUTH_JSON_ADMIN;
import static com.danarim.monal.DbUserFiller.AUTH_JSON_USER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Extension methods for MockMvc.
 * <br>
 * Add 'secured' flag to requests and set default headers.
 */
public final class TestUtils {

    /**
     * Add 'secured' flag to get request.
     * @param uri request uri
     * @return request builder
     */
    public static MockHttpServletRequestBuilder getExt(String uri) {
        return get(uri).secure(true);
    }

    /**
     * Perform login request and return get request builder with access token and csrf.
     * @param uri request uri
     * @param userRole user role for auth
     * @param mockMvc mock mvc. Used for login request.
     * @return request builder
     * @throws Exception if login request or data parsing failed
     */
    public static MockHttpServletRequestBuilder getExtWithAuth(String uri,
                                                               RoleName userRole,
                                                               MockMvc mockMvc
    ) throws Exception {

        String loginJson;
        if (userRole == RoleName.ROLE_ADMIN) {
            loginJson = AUTH_JSON_ADMIN;
        } else {
            loginJson = AUTH_JSON_USER;
        }
        MvcResult result = mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        AuthResponseEntity authResponse = new ObjectMapper().readValue(json, AuthResponseEntity.class);

        Cookie accessTokenCookie = result.getResponse().getCookie(JwtUtil.KEY_ACCESS_TOKEN);
        String csrfToken = authResponse.csrfToken();

        return getExt(uri)
                .header("X-CSRF-TOKEN", csrfToken)
                .cookie(accessTokenCookie);
    }

    public static MockHttpServletRequestBuilder postExt(String uri) {
        return post(uri).secure(true);
    }

    /**
     * Add default headers to request.
     * @param uri request uri
     * @param body request body
     * @return request builder that can be extended
     */
    public static MockHttpServletRequestBuilder postExt(String uri, String body) {
        return post(uri)
                .content(body)
                .contentType(APPLICATION_JSON)
                .characterEncoding(UTF_8)
                .accept(APPLICATION_JSON)
                .secure(true);
    }

    /**
     * Add default headers to request and parse body to json.
     * @param uri request uri
     * @param body request body
     * @return request builder that can be extended
     */
    public static MockHttpServletRequestBuilder postExt(String uri, Object body) {
        try {
            String json = new ObjectMapper().writeValueAsString(body);

            return postExt(uri, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
