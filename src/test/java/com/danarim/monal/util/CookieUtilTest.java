package com.danarim.monal.util;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class CookieUtilTest {

    @Test
    void testGetCookieValueByRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        try (MockedStatic<WebUtils> webUtilsMockedStatic = mockStatic(WebUtils.class)) {
            webUtilsMockedStatic.when(() ->
                    WebUtils.getCookie(request, "cookieName")
            ).thenReturn(new Cookie("cookieName", "cookieValue"));

            String result = CookieUtil.getCookieValueByRequest(request, "cookieName");

            assertEquals("cookieValue", result);
        }
    }

    @Test
    void testGetCookieValueByRequestNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        try (MockedStatic<WebUtils> webUtilsMockedStatic = mockStatic(WebUtils.class)) {
            webUtilsMockedStatic.when(() ->
                    WebUtils.getCookie(request, "cookieName")
            ).thenReturn(null);

            String result = CookieUtil.getCookieValueByRequest(request, "cookieName");

            assertNull(result);
        }
    }

    @Test
    void createAccessTokenCookie() {
        Cookie result = CookieUtil.createAccessTokenCookie("accessToken");

        assertEquals("accessToken", result.getValue());
        assertTrue(result.isHttpOnly(), "accessToken cookie should be httpOnly");
        assertTrue(result.getSecure(), "accessToken cookie should be secure");
        assertEquals("/", result.getPath(), "accessToken cookie should be set to root path");
        assertTrue(result.getMaxAge() > 0, "Max age should be greater than 0");
    }

    @Test
    void createRefreshTokenCookie() {
        Cookie result = CookieUtil.createRefreshTokenCookie("refreshToken");

        assertEquals("refreshToken", result.getValue());
        assertTrue(result.isHttpOnly(), "refreshToken cookie should be httpOnly");
        assertTrue(result.getSecure(), "refreshToken cookie should be secure");
        assertEquals("/", result.getPath(), "refreshToken cookie path should be set to root path");
        assertTrue(result.getMaxAge() > 0, "Max age should be greater than 0");
    }

    @Test
    void deleteAccessTokenCookie() {
        Cookie result = CookieUtil.deleteAccessTokenCookie();

        assertNull(result.getValue(), "accessToken cookie value should be null after deletion");
        assertTrue(result.isHttpOnly(), "accessToken cookie should be httpOnly");
        assertEquals("/", result.getPath(), "accessToken cookie should be set to root path");
        assertEquals(0, result.getMaxAge(), "Max age should be 0 after deletion");
    }

    @Test
    void deleteRefreshTokenCookie() {
        Cookie result = CookieUtil.deleteRefreshTokenCookie();

        assertNull(result.getValue(), "refreshToken cookie value should be null after deletion");
        assertTrue(result.isHttpOnly(), "refreshToken cookie should be httpOnly");
        assertEquals("/", result.getPath(), "refreshToken cookie path should be set to root path");
        assertEquals(0, result.getMaxAge(), "Max age should be 0 after deletion");
    }

    @Test
    void createAppMessageCookie() {
        //Can`t test for JsonProcessingException because it is a checked exception
        ApplicationMessage applicationMessage = new ApplicationMessage(
                "message",
                ApplicationMessageType.INFO,
                "page",
                "actionCode"
        );
        Cookie result = CookieUtil.createAppMessageCookie(applicationMessage);

        assertNotNull(result.getValue(), "appMessage cookie value should not be null");
        assertFalse(result.isHttpOnly(), "appMessage cookie should not be httpOnly");
        assertEquals("/", result.getPath(), "appMessage cookie should be set to root path");
        assertTrue(result.getMaxAge() > 0, "Max age should be greater than 0");
    }
}
