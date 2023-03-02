package com.danarim.monal.user.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.failhandler.ResponseErrorType;
import com.danarim.monal.failhandler.RestExceptionHandler;
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

import static com.danarim.monal.TestUtils.postExt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@ContextConfiguration(classes = {RegistrationController.class, RestExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @MockBean
    private ApplicationListener<ApplicationEvent> listener;
    @MockBean(name = "messageSource")
    private MessageSource messages;

    @MockBean
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        configurableApplicationContext.addApplicationListener(listener);
    }

    @Test
    void registration() throws Exception {
        RegistrationDto registrationDto = new RegistrationDto(
                "John", "Doe",
                "test1234", "test1234",
                "test@test.test"
        );
        when(registrationService.registerNewUserAccount(registrationDto))
                .thenReturn(mock(User.class));

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
                .andExpect(status().isCreated());

        verify(registrationService).registerNewUserAccount(registrationDto);
        verify(listener).onApplicationEvent(any(OnRegistrationCompleteEvent.class));
    }

    @Test
    void registration_InvalidData_BadRequest() throws Exception {
        RegistrationDto registrationDto = new RegistrationDto(
                "John", "Doe",
                "test1234", "test1234",
                "invalid"
        );
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$[0].type")
                                   .value(ResponseErrorType.FIELD_VALIDATION_ERROR.getName()))
                .andExpect(jsonPath("$[0].fieldName").value("email"))
                .andExpect(jsonPath("$[0].message").exists());

        verify(registrationService, never()).registerNewUserAccount(registrationDto);
    }

    @Test
    void resendVerificationToken() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resendVerificationToken")
                                .param("email", "someEmail"))
                .andExpect(status().isNoContent());

        verify(registrationService).resendVerificationEmail("someEmail");
    }

    @Test
    void resetPassword() throws Exception {
        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPassword")
                                .param("email", "someEmail"))
                .andExpect(status().isNoContent());

        verify(registrationService).resetPassword("someEmail");
    }

    @Test
    void resetPasswordSet() throws Exception {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("12345678", "12345678");
        User user = mock(User.class);

        Cookie tokenCookie = CookieUtil.createPasswordResetCookie("someToken");

        when(registrationService.updateForgottenPassword(
                eq(resetPasswordDto), anyString()))
                .thenReturn(user);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPasswordSet", resetPasswordDto)
                                .cookie(tokenCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("passwordResetToken"))
                .andExpect(cookie().maxAge("passwordResetToken", 0));

        verify(registrationService).updateForgottenPassword(resetPasswordDto, "someToken");
        verify(listener).onApplicationEvent(any(OnPasswordUpdatedEvent.class));
    }

    @Test
    void resetPasswordSet_InvalidData_BadRequest() throws Exception {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("123", "123");

        Cookie tokenCookie = CookieUtil.createPasswordResetCookie("someToken");

        when(registrationService.updateForgottenPassword(
                eq(resetPasswordDto), anyString()))
                .thenThrow(BadFieldException.class);

        when(messages.getMessage(any(), any(), any()))
                .thenReturn("error");

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPasswordSet", resetPasswordDto)
                                .cookie(tokenCookie))
                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$[0].type")
                                   .value(ResponseErrorType.FIELD_VALIDATION_ERROR.getName()))
                .andExpect(jsonPath("$[0].fieldName").value("newPassword"))
                .andExpect(jsonPath("$[0].message").exists());

        verify(registrationService, never()).updateForgottenPassword(resetPasswordDto, "someToken");
        verify(listener, never()).onApplicationEvent(any(OnPasswordUpdatedEvent.class));
    }

    @Test
    void resetPasswordSet_InvalidToken_BadRequest() throws Exception {
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto("12345678", "12345678");

        Cookie tokenCookie = CookieUtil.createPasswordResetCookie("someToken");

        when(registrationService.updateForgottenPassword(
                eq(resetPasswordDto), anyString()))
                .thenThrow(InvalidTokenException.class);

        when(messages.getMessage(any(), any(), any()))
                .thenReturn("error");

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/resetPasswordSet", resetPasswordDto)
                                .cookie(tokenCookie))
                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$[0].type")
                                   .value(ResponseErrorType.GLOBAL_ERROR.getName()))
                .andExpect(jsonPath("$[0].message").exists());

        verify(registrationService).updateForgottenPassword(resetPasswordDto, "someToken");
        verify(listener, never()).onApplicationEvent(any(OnPasswordUpdatedEvent.class));
    }

}
