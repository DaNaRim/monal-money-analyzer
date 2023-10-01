package com.danarim.monal;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.auth.AuthResponseEntity;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.util.CookieUtil;
import com.danarim.monal.util.appmessage.AppMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.Cookie;

import static com.danarim.monal.DbUserFiller.AUTH_JSON_ADMIN;
import static com.danarim.monal.DbUserFiller.AUTH_JSON_USER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Utility class to help with testing and optimize code. Contains methods to perform requests and
 * get results.
 */
public final class TestUtils {

    /*
        Request extensions
     */

    /**
     * Add 'secured' flag to get request.
     *
     * @param uri request uri
     *
     * @return request builder. Can be extended
     */
    public static MockHttpServletRequestBuilder getExt(String uri) {
        return get(uri).secure(true);
    }

    /**
     * Perform login request for user with matched role and return 'get' request builder with
     * access, refresh tokens and csrf.
     *
     * @param uri      request uri
     * @param userRole user role for auth
     * @param mockMvc  mock mvc. Used for login request.
     *
     * @return request builder. Can be extended
     *
     * @throws Exception if login request or data parsing failed
     * @see #getExt(String)
     */
    public static MockHttpServletRequestBuilder getExtWithAuth(String uri,
                                                               RoleName userRole,
                                                               MockMvc mockMvc
    ) throws Exception {

        MvcResult result = getLoginResult(userRole, mockMvc);

        return getExt(uri)
                .headers(csrfTokenHeader(result))
                .cookie(getAccessTokenCookie(result))
                .cookie(getRefreshTokenCookie(result));
    }

    /**
     * Add 'secured' flag to post request.
     *
     * @param uri request uri
     *
     * @return request builder. Can be extended
     */
    public static MockHttpServletRequestBuilder postExt(String uri) {
        return post(uri).secure(true);
    }

    /**
     * Add default headers to request.
     *
     * @param uri  request uri
     * @param body request body in json format
     *
     * @return request builder. Can be extended
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
     *
     * @param uri  request uri
     * @param body request body
     *
     * @return request builder. Can be extended
     *
     * @throws RuntimeException if body parsing failed
     * @see #postExt(String, String)
     */
    public static MockHttpServletRequestBuilder postExt(String uri, Object body) {
        try {
            String json = new ObjectMapper().writeValueAsString(body);

            return postExt(uri, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add default headers to request and perform login request for user with matched role.
     *
     * @param uri      request uri
     * @param body     request body
     * @param userRole user role for auth
     * @param mockMvc  mock mvc. Used for login request.
     *
     * @return request builder. Can be extended
     *
     * @throws Exception if login request or data parsing failed
     */
    public static MockHttpServletRequestBuilder postExtWithAuth(String uri,
                                                                Object body,
                                                                RoleName userRole,
                                                                MockMvc mockMvc
    ) throws Exception {

        MvcResult result = getLoginResult(userRole, mockMvc);

        return postExt(uri, body)
                .headers(csrfTokenHeader(result))
                .cookie(getAccessTokenCookie(result))
                .cookie(getRefreshTokenCookie(result));
    }

    /**
     * Add 'secured' flag to delete request.
     *
     * @param uri request uri
     *
     * @return request builder. Can be extended
     */
    public static MockHttpServletRequestBuilder deleteExt(String uri) {
        return delete(uri).secure(true);
    }

    /*
        Other utils
     */

    /**
     * Perform login request for user with matched role and return result.
     *
     * @param userRole user role for auth
     * @param mockMvc  mock mvc. Used for login request.
     *
     * @return login result
     *
     * @throws AssertionError if login request not working correctly
     * @throws Exception      if login request failed
     * @see #getExtWithAuth(String, RoleName, MockMvc)
     * @see #postExtWithAuth(String, Object, RoleName, MockMvc)
     */
    public static MvcResult getLoginResult(RoleName userRole, MockMvc mockMvc) throws Exception {
        String loginJson;
        if (userRole == RoleName.ROLE_ADMIN) {
            loginJson = AUTH_JSON_ADMIN;
        } else {
            loginJson = AUTH_JSON_USER;
        }
        return mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/login", loginJson))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CookieUtil.COOKIE_ACCESS_TOKEN_KEY))
                .andExpect(cookie().exists(CookieUtil.COOKIE_REFRESH_TOKEN_KEY))
                .andReturn();
    }

    /**
     * Get access token cookie from mvc result.
     *
     * @param mvcResult result of request
     *
     * @return access token cookie
     *
     * @throws AssertionError if access token cookie not found
     */
    public static Cookie getAccessTokenCookie(MvcResult mvcResult) {
        Cookie accessTokenCookie =
                mvcResult.getResponse().getCookie(CookieUtil.COOKIE_ACCESS_TOKEN_KEY);
        assertNotNull(accessTokenCookie);
        return accessTokenCookie;
    }

    /**
     * Get refresh token cookie from mvc result.
     *
     * @param mvcResult result of request
     *
     * @return refresh token cookie
     *
     * @throws AssertionError if refresh token cookie not found
     */
    public static Cookie getRefreshTokenCookie(MvcResult mvcResult) {
        Cookie refreshTokenCookie =
                mvcResult.getResponse().getCookie(CookieUtil.COOKIE_REFRESH_TOKEN_KEY);
        assertNotNull(refreshTokenCookie);
        return refreshTokenCookie;
    }

    /**
     * Get application message cookie from mvc result, parse it and return as ApplicationMessage.
     *
     * @param mvcResult result of request
     *
     * @return application message object from result
     *
     * @throws AssertionError          if application message cookie not found
     * @throws JsonProcessingException if cookie parsing failed
     */
    public static AppMessage getAppMessage(MvcResult mvcResult)
            throws JsonProcessingException {
        Cookie appMessageCookie =
                mvcResult.getResponse().getCookie(CookieUtil.COOKIE_APP_MESSAGE_KEY);

        assertNotNull(appMessageCookie, "ApplicationMessage cookie is null");

        return new ObjectMapper().readValue(appMessageCookie.getValue(), AppMessage.class);
    }

    /**
     * Get password reset token cookie from mvc result.
     *
     * @param mvcResult result of request
     *
     * @return password reset token cookie
     *
     * @throws AssertionError if password reset token cookie not found
     */
    public static Cookie getPasswordResetTokenCookie(MvcResult mvcResult) {
        Cookie passwordResetCookie =
                mvcResult.getResponse().getCookie(CookieUtil.COOKIE_PASSWORD_RESET_TOKEN_KEY);
        assertNotNull(passwordResetCookie);
        return passwordResetCookie;
    }

    /**
     * Get csrf token header from mvc result and return it as HttpHeaders. You can use it to add
     * csrf token to further requests.
     * <br>
     * Example:
     * <pre>
     *     MvcResult result = getLoginResult(RoleName.ROLE_ADMIN, mockMvc);
     *
     *     mockMvc.perform(...)
     *          .headers(csrfTokenHeader(result))
     * </pre>
     *
     * @param mvcResult result of request
     *
     * @return csrf token header
     *
     * @throws UnsupportedEncodingException if something went wrong during csrf token parsing
     * @throws JsonProcessingException      if something went wrong during csrf token parsing
     * @see ObjectMapper#readValue(String, Class)
     */
    public static HttpHeaders csrfTokenHeader(MvcResult mvcResult)
            throws UnsupportedEncodingException, JsonProcessingException {

        String json = mvcResult.getResponse().getContentAsString();
        AuthResponseEntity authResponse =
                new ObjectMapper().readValue(json, AuthResponseEntity.class);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-CSRF-TOKEN", authResponse.csrfToken());
        return headers;
    }

}
