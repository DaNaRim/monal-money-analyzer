package com.danarim.monal.user.web.controller;

import com.danarim.monal.TestContainersConfig;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.user.persistence.dao.TokenDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.user.web.dto.ResetPasswordDto;
import com.danarim.monal.util.appmessage.AppMessage;
import com.danarim.monal.util.appmessage.AppMessageType;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;

import static com.danarim.monal.TestUtils.getAppMessage;
import static com.danarim.monal.TestUtils.getExt;
import static com.danarim.monal.TestUtils.getPasswordResetTokenCookie;
import static com.danarim.monal.TestUtils.postExt;
import static com.danarim.monal.user.web.controller.TokensIT.TokensITUtils.getTokenValueFromEmail;
import static com.danarim.monal.user.web.controller.TokensIT.TokensITUtils.registerNewUser;
import static com.danarim.monal.user.web.controller.TokensIT.TokensITUtils.resetPassword;
import static com.danarim.monal.user.web.controller.TokensIT.TokensITUtils.resetPasswordConfirm;
import static com.danarim.monal.user.web.controller.TokensIT.TokensITUtils.resetPasswordSet;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
class TokensIT {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.aConfig().withUser("testUsername", "testPassword"))
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
    void verificationToken() throws Exception {
        String userEmail = "testVerificationToken@email.com";

        registerNewUser(userEmail, mockMvc);

        String tokenValue = getTokenValueFromEmail(0);

        MvcResult regConfirmResult =
                mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/registrationConfirm")
                                        .param("token", tokenValue))
                        .andExpect(status().is3xxRedirection())
                        .andReturn();

        AppMessage appMessage = getAppMessage(regConfirmResult);

        Token token = tokenDao.findByTokenValue(tokenValue);
        User user = userDao.findByEmailIgnoreCase(userEmail);

        assertSame(AppMessageType.INFO, appMessage.type(),
                   "ApplicationMessage type is not INFO, maybe exception was thrown during "
                           + "activation");
        assertTrue(user.isEnabled(), "User should be enabled after verification");
        assertTrue(token.isUsed(), "Token should be marked as used after verification");
    }

    @Test
    void verificationTokenResend() throws Exception {
        String userEmail = "verificationTokenResend@email";

        registerNewUser(userEmail, mockMvc);

        String tokenValue = getTokenValueFromEmail(0);

        MvcResult regConfirmResult =
                mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/registrationConfirm")
                                        .param("token", tokenValue))
                        .andExpect(status().is3xxRedirection())
                        .andReturn();

        AppMessage appMessage = getAppMessage(regConfirmResult);

        Token token = tokenDao.findByTokenValue(tokenValue);
        User user = userDao.findByEmailIgnoreCase(userEmail);

        assertSame(AppMessageType.INFO, appMessage.type(),
                   "ApplicationMessage type is not INFO, maybe exception was thrown during "
                           + "activation");
        assertTrue(user.isEnabled(), "User should be enabled after verification");
        assertTrue(token.isUsed(), "Token should be marked as used after verification");
    }

    @Test
    void passwordResetToken() throws Exception {
        String userEmail = "testPasswordResetToken@email.com";

        registerNewUser(userEmail, mockMvc);
        resetPassword(userEmail, mockMvc);

        String tokenValue = getTokenValueFromEmail(1); // 0 - verification, 1 - password reset

        MvcResult resetConfirmResult = resetPasswordConfirm(tokenValue, mockMvc);
        Cookie resetPassCookie = getPasswordResetTokenCookie(resetConfirmResult);

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("newPassword", "newPassword");
        resetPasswordSet(resetPasswordDto, resetPassCookie, mockMvc);

        User user = userDao.findByEmailIgnoreCase(userEmail);
        Token token = tokenDao.findByTokenValue(tokenValue);

        assertTrue(passwordEncoder.matches(resetPasswordDto.password(), user.getPassword()),
                   "Password was not changed");
        assertTrue(token.isUsed(), "Token should be marked as used after verification");
    }

    protected static class TokensITUtils {

        protected static String getTokenValueFromEmail(int emailPosition) {
            assertTrue(greenMail.waitForIncomingEmail(5000, 1), "No email was received");

            String emailBody =
                    GreenMailUtil.getBody(greenMail.getReceivedMessages()[emailPosition])
                            .replaceAll("=3D", "") //=3D - encoded '='. Goes after 'token='
                            .replaceAll("\\r?\\n", "");

            int tokenStartIndex = emailBody.indexOf("token=") + 6;
            return emailBody.substring(tokenStartIndex, tokenStartIndex + 36); //36 - UUID length
        }

        protected static void registerNewUser(String email, MockMvc mockMvc) throws Exception {
            RegistrationDto registrationDto = new RegistrationDto(
                    "John", "Doe",
                    "test1234", "test1234",
                    email
            );
            mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
                    .andExpect(status().isCreated());
        }

        protected static void resetPassword(String email, MockMvc mockMvc) throws Exception {
            mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPassword")
                                    .param("email", email))
                    .andExpect(status().isNoContent())
                    .andReturn();
        }

        protected static MvcResult resetPasswordConfirm(String tokenValue, MockMvc mockMvc)
                throws Exception {
            return mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/resetPasswordConfirm")
                                           .param("token", tokenValue))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();
        }

        protected static void resetPasswordSet(ResetPasswordDto resetPassDto,
                                               Cookie resetPassCookie,
                                               MockMvc mockMvc
        ) throws Exception {
            mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPasswordSet", resetPassDto)
                                    .cookie(resetPassCookie))
                    .andExpect(status().isNoContent())
                    .andReturn();
        }

    }

}
