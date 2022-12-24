package com.danarim.monal.user.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.failHandler.GlobalExceptionHandler;
import com.danarim.monal.failHandler.ResponseErrorType;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.TokenType;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.service.event.OnPasswordUpdatedEvent;
import com.danarim.monal.user.service.event.OnRegistrationCompleteEvent;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.user.web.dto.ResetPasswordDto;
import com.danarim.monal.util.CookieUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;
import java.util.Collections;
import java.util.Date;

import static com.danarim.monal.TestUtils.getExt;
import static com.danarim.monal.TestUtils.postExt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@ContextConfiguration(classes = {RegistrationController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @MockBean
    private ApplicationListener<ApplicationEvent> listener;

    @MockBean
    private RegistrationService registrationService;

    @MockBean(name = "messageSource")
    private MessageSource messages;

    @BeforeEach
    void setUp() {
        configurableApplicationContext.addApplicationListener(listener);
    }

    @Test
    void testRegisterUser() throws Exception {

        RegistrationDto registrationDto = new RegistrationDto(
                "John", "Doe",
                "test1234", "test1234",
                "test@test.test"
        );
        when(registrationService.registerNewUserAccount(registrationDto))
                .thenReturn(new User("t", "e", "s", "t", new Date(), Collections.emptySet()));

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
                .andExpect(status().isCreated());

        verify(registrationService).registerNewUserAccount(registrationDto);
        verify(listener).onApplicationEvent(any(OnRegistrationCompleteEvent.class));
    }

    @Test
    void testRegisterUserInvalidData() throws Exception {
        RegistrationDto registrationDto = new RegistrationDto(
                "John", "Doe",
                "test1234", "test1234",
                "invalid"
        );

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$[0].type").value(ResponseErrorType.FIELD_VALIDATION_ERROR.getName()))
                .andExpect(jsonPath("$[0].fieldName").value("email"))
                .andExpect(jsonPath("$[0].message").exists());

        verify(registrationService, never()).registerNewUserAccount(registrationDto);
    }

    @Test
    void testConfirmRegistration() throws Exception {
        when(messages.getMessage(anyString(), any(), any())).thenReturn("test");

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/registrationConfirm?token=someToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(cookie().exists("serverMessage"));

        verify(registrationService).confirmRegistration("someToken");
    }

    @Test
    void testConfirmRegistrationInvalidToken() throws Exception {
        doThrow(new InvalidTokenException("t", "validation.token.invalid", null))
                .when(registrationService).confirmRegistration(anyString());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/registrationConfirm?token=someToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(cookie().exists("serverMessage"));
    }

    @Test
    void testResendVerificationToken() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resendVerificationToken?email=someEmail"))
                .andExpect(status().isNoContent());

        verify(registrationService).resendVerificationEmail("someEmail");
    }

    @Test
    void testResetPassword() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPassword?email=someEmail"))
                .andExpect(status().isNoContent());

        verify(registrationService).resetPassword("someEmail");
    }

    @Test
    void testResetPasswordConfirm() throws Exception {
        Token passwordResetToken = new Token(mock(User.class), TokenType.PASSWORD_RESET);

        when(registrationService.validatePasswordResetToken(anyString()))
                .thenReturn(passwordResetToken);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/resetPasswordConfirm?token=someToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/resetPasswordSet"))
                .andExpect(cookie().exists("passwordResetToken"));

        verify(registrationService).validatePasswordResetToken("someToken");
    }

    @Test
    void testResetPasswordConfirmInvalidToken() throws Exception {
        doThrow(new InvalidTokenException("t", "validation.token.invalid", null))
                .when(registrationService).validatePasswordResetToken(anyString());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/resetPasswordConfirm?token=someToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(cookie().exists("serverMessage"));

        verify(registrationService).validatePasswordResetToken("someToken");
    }

    @Test
    void testResetPasswordSet() throws Exception {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("12345678", "12345678");
        User user = mock(User.class);

        Cookie tokenCookie = CookieUtil.createPasswordResetCookie("someToken");

        when(registrationService.updateForgottenPassword(eq(resetPasswordDto), anyString()))
                .thenReturn(user);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPasswordSet", resetPasswordDto)
                        .cookie(tokenCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("passwordResetToken"))
                .andExpect(cookie().maxAge("passwordResetToken", 0));

        verify(registrationService).updateForgottenPassword(resetPasswordDto, "someToken");
        verify(listener).onApplicationEvent(any(OnPasswordUpdatedEvent.class));
    }
}
