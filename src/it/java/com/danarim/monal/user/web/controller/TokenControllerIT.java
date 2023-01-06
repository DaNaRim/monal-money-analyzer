package com.danarim.monal.user.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.failHandler.ViewExceptionHandler;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.TokenType;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.danarim.monal.TestUtils.getExt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TokenController.class)
@ContextConfiguration(classes = {TokenController.class, ViewExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class TokenControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;
    @MockBean
    private TokenService tokenService;
    @MockBean(name = "messageSource")
    private MessageSource messages;

    @Test
    void registrationConfirm() throws Exception {
        when(messages.getMessage(anyString(), any(), any())).thenReturn("test");

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/registrationConfirm")
                        .param("token", "someToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(cookie().exists("serverMessage"));

        verify(registrationService).confirmRegistration("someToken");
    }


    @Test
    void registrationConfirm_InvalidToken_ErrorInAppMesCookie() throws Exception {
        doThrow(new InvalidTokenException("t", "validation.token.not-found", null))
                .when(registrationService).confirmRegistration(anyString());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/registrationConfirm")
                        .param("token", "someToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(cookie().exists("serverMessage"));
    }


    @Test
    void resetPasswordConfirm() throws Exception {
        Token passwordResetToken = new Token(mock(User.class), TokenType.PASSWORD_RESET);

        when(tokenService.validatePasswordResetToken(anyString()))
                .thenReturn(passwordResetToken);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/resetPasswordConfirm")
                        .param("token", "someToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/resetPasswordSet"))
                .andExpect(cookie().exists("passwordResetToken"));

        verify(tokenService).validatePasswordResetToken("someToken");
    }

    @Test
    void resetPasswordConfirm_InvalidToken_ErrorInAppMesCookie() throws Exception {
        doThrow(new InvalidTokenException("t", "validation.token.not-found", null))
                .when(tokenService).validatePasswordResetToken(anyString());

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/resetPasswordConfirm")
                        .param("token", "someToken"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(cookie().exists("serverMessage"));

        verify(tokenService).validatePasswordResetToken("someToken");
    }

}