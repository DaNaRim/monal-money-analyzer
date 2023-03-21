package com.danarim.monal.util;

import com.danarim.monal.util.appmessage.AppMessage;
import com.danarim.monal.util.appmessage.AppMessageCode;
import com.danarim.monal.util.appmessage.AppMessageType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class CookieUtilTest {

    @Test
    void getCookieValueByRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        try (MockedStatic<WebUtils> webUtilsMockedStatic = mockStatic(WebUtils.class)) {

            webUtilsMockedStatic.when(() -> WebUtils.getCookie(request, "cookieName"))
                    .thenReturn(new Cookie("cookieName", "cookieValue"));

            String result = CookieUtil.getCookieValueByRequest(request, "cookieName");

            assertEquals("cookieValue", result);
        }
    }

    @Test
    void getCookieValueByRequest_NotFound_Null() {
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
        assertEquals("/", result.getPath(), "accessToken cookie should be set to root path");
        assertTrue(result.isHttpOnly(), "accessToken cookie should be httpOnly");
        assertTrue(result.getSecure(), "accessToken cookie should be secure");
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
    void createRefreshTokenCookie() {
        Cookie result = CookieUtil.createRefreshTokenCookie("refreshToken");

        assertEquals("refreshToken", result.getValue());
        assertEquals("/", result.getPath(), "refreshToken cookie path should be set to root path");
        assertTrue(result.isHttpOnly(), "refreshToken cookie should be httpOnly");
        assertTrue(result.getSecure(), "refreshToken cookie should be secure");
        assertTrue(result.getMaxAge() > 0, "Max age should be greater than 0");
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
    void createAuthInitCookie() {
        Cookie result = CookieUtil.createAuthInitCookie();

        assertEquals("/", result.getPath(), "authInit cookie should be set to root path");
        assertTrue(result.getMaxAge() > 0, "Max age should be greater than 0");
    }

    @Test
    void deleteAuthInitCookie() {
        Cookie result = CookieUtil.deleteAuthInitCookie();

        assertNull(result.getValue(), "authInit cookie value should be null after deletion");
        assertEquals("/", result.getPath(), "authInit cookie should be set to root path");
        assertEquals(0, result.getMaxAge(), "Max age should be 0 after deletion");
    }

    @Test
    void createAppMessageCookie() {
        //Can`t test for JsonProcessingException because it is a checked exception
        AppMessage applicationMessage = new AppMessage(
                AppMessageType.INFO,
                "page",
                AppMessageCode.TOKEN_NOT_FOUND
        );
        Cookie result = CookieUtil.createAppMessageCookie(applicationMessage);

        assertNotNull(result.getValue(), "appMessage cookie value should not be null");
        assertEquals("/", result.getPath(), "appMessage cookie should be set to root path");
        assertFalse(result.isHttpOnly(), "appMessage cookie should not be httpOnly");
        assertTrue(result.getMaxAge() > 0, "Max age should be greater than 0");
    }

    @Test
    void createPasswordResetCookie() {
        Cookie result = CookieUtil.createPasswordResetCookie("passwordResetToken");

        assertEquals("passwordResetToken", result.getValue());
        assertEquals("/", result.getPath(), "passwordResetToken cookie should be set to root path");
        assertTrue(result.isHttpOnly(), "passwordResetToken cookie should be httpOnly");
        assertTrue(result.getSecure(), "passwordResetToken cookie should be secure");
        assertTrue(result.getMaxAge() > 0, "Max age should be greater than 0");
    }

    @Test
    void deletePasswordResetCookie() {
        Cookie result = CookieUtil.deletePasswordResetCookie();

        assertNull(result.getValue(),
                   "passwordResetToken cookie value should be null after deletion");
        assertTrue(result.isHttpOnly(), "passwordResetToken cookie should be httpOnly");
        assertEquals("/", result.getPath(), "passwordResetToken cookie should be set to root path");
        assertEquals(0, result.getMaxAge(), "Max age should be 0 after deletion");
    }

}
