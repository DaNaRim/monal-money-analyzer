package com.danarim.monal.user.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.user.persistence.dao.TokenDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.user.web.dto.ResetPasswordDto;
import com.danarim.monal.util.ApplicationMessage;
import com.danarim.monal.util.ApplicationMessageType;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.Cookie;

import static com.danarim.monal.TestUtils.getExt;
import static com.danarim.monal.TestUtils.postExt;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Test tokens functionality with email")
class TestTokensIT {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("testUsername", "testPassword"))
            .withPerMethodLifecycle(true);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenDao tokenDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testVerificationToken() throws Exception {
        RegistrationDto registrationDto = new RegistrationDto(
                "John", "Doe",
                "test1234", "test1234",
                "testVerificationToken@email.com"
        );
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
                .andExpect(status().isCreated());

        assertTrue(greenMail.waitForIncomingEmail(5000, 1), "No email was received");

        String emailBody = greenMail.getReceivedMessages()[0].getContent().toString();

        int tokenStartIndex = emailBody.indexOf("token=") + 6;
        String tokenValue = emailBody.substring(tokenStartIndex, tokenStartIndex + 36); //36 - UUID length

        MvcResult mvcResult = mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/registrationConfirm")
                        .param("token", tokenValue))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie cookie = mvcResult.getResponse().getCookie("serverMessage");
        assertNotNull(cookie, "ApplicationMessage cookie is null");

        ApplicationMessage appMessage = new ObjectMapper().readValue(cookie.getValue(), ApplicationMessage.class);

        assertSame(ApplicationMessageType.INFO, appMessage.type(),
                "ApplicationMessage type is not INFO, maybe exception was thrown during activation"
        );
        Token token = tokenDao.findByTokenValue(tokenValue);
        User user = userDao.findByEmailIgnoreCase(registrationDto.email());
        assertNull(token, "Token should be deleted after verification");
        assertTrue(user.isEnabled(), "User should be enabled after verification");
    }

    @Test
    void testVerificationTokenResend() throws Exception {
        RegistrationDto registrationDto = new RegistrationDto(
                "John", "Doe",
                "test1234", "test1234",
                "testVerificationTokenResend@email.com"
        );
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
                .andExpect(status().isCreated());

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resendVerificationToken")
                        .param("email", registrationDto.email()))
                .andExpect(status().isNoContent());

        assertTrue(greenMail.waitForIncomingEmail(5000, 1), "No email was received");

        String emailBody = greenMail.getReceivedMessages()[1].getContent().toString();

        int tokenStartIndex = emailBody.indexOf("token=") + 6;
        String tokenValue = emailBody.substring(tokenStartIndex, tokenStartIndex + 36); //36 - UUID length

        MvcResult mvcResult = mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/registrationConfirm")
                        .param("token", tokenValue))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie cookie = mvcResult.getResponse().getCookie("serverMessage");
        assertNotNull(cookie, "ApplicationMessage cookie is null");

        ApplicationMessage appMessage = new ObjectMapper().readValue(cookie.getValue(), ApplicationMessage.class);

        assertSame(ApplicationMessageType.INFO, appMessage.type(),
                "ApplicationMessage type is not INFO, maybe exception was thrown during activation"
        );
        Token token = tokenDao.findByTokenValue(tokenValue);
        User user = userDao.findByEmailIgnoreCase(registrationDto.email());
        assertNull(token, "Token should be deleted after verification");
        assertTrue(user.isEnabled(), "User should be enabled after verification");
    }

    @Test
    void testPasswordResetToken() throws Exception { //TODO check if able split into smaller tests
        String email = "testPasswordResetToken@email.com";

        RegistrationDto registrationDto = new RegistrationDto(
                "John", "Doe",
                "test1234", "test1234",
                email
        );
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
                .andExpect(status().isCreated());

        //reset password begins

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPassword")
                        .param("email", email));

        assertTrue(greenMail.waitForIncomingEmail(5000, 1), "No email was received");

        String emailBody = greenMail.getReceivedMessages()[1].getContent().toString(); // 0 - verifacation, 1 - pass reset

        int tokenStartIndex = emailBody.indexOf("token=") + 6;
        String tokenValue = emailBody.substring(tokenStartIndex, tokenStartIndex + 36); //36 - UUID length

        //reset password confirm

        MvcResult resetConfirmResult = mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/resetPasswordConfirm")
                        .param("token", tokenValue))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        Cookie resetPassCookie = resetConfirmResult.getResponse().getCookie("passwordResetToken");

        //set new password

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("newPassword", "newPassword");

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPasswordSet", resetPasswordDto)
                        .cookie(resetPassCookie))
                .andExpect(status().isNoContent());

        //check if password was changed
        User user = userDao.findByEmailIgnoreCase(email);
        assertTrue(passwordEncoder.matches(resetPasswordDto.password(), user.getPassword()));

        //check if token was deleted
        Token token = tokenDao.findByTokenValue(tokenValue);
        assertNull(token, "Token should be deleted after password reset");
    }
}
