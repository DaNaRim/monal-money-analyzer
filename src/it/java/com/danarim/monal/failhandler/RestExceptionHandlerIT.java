package com.danarim.monal.failhandler;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.controller.StubController;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.danarim.monal.TestUtils.getExt;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StubController.class)
@ContextConfiguration(classes = {StubController.class, RestExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class RestExceptionHandlerIT {

    private static final LogCaptor logCaptor = LogCaptor.forClass(RestExceptionHandler.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "messageSource")
    private MessageSource messages;

    @AfterAll
    public static void tearDown() {
        logCaptor.close();
    }

    @AfterEach
    public void clearLogs() {
        logCaptor.clearLogs();
    }

    @Test
    void handleBadRequestException() throws Exception {
        when(messages.getMessage(anyString(), any(), any()))
                .thenReturn("test");

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/badRequestStub"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].type")
                                   .value(ResponseErrorType.GLOBAL_ERROR.getName()))
                .andExpect(jsonPath("$[0].fieldName")
                                   .value(ResponseErrorType.GLOBAL_ERROR.getName()))
                .andExpect(jsonPath("$[0].message").value("test"))
                .andExpect(jsonPath("$[0].errorCode").exists());

        assertThat(logCaptor.getDebugLogs()).hasSize(1);
    }

    @Test
    void handleBadFieldException() throws Exception {
        when(messages.getMessage(anyString(), any(), any()))
                .thenReturn("test");

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/badFieldStub"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].type")
                                   .value(ResponseErrorType.FIELD_VALIDATION_ERROR.getName()))
                .andExpect(jsonPath("$[0].fieldName").value("field"))
                .andExpect(jsonPath("$[0].message").value("test"))
                .andExpect(jsonPath("$[0].errorCode").exists());

        assertThat(logCaptor.getDebugLogs()).hasSize(1);
    }

    @Test
    void handleAccessDeniedException() throws Exception {
        when(messages.getMessage(anyString(), any(), any()))
                .thenReturn("test");

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/accessDeniedStub"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$[0].type")
                                   .value(ResponseErrorType.GLOBAL_ERROR.getName()))
                .andExpect(jsonPath("$[0].fieldName")
                                   .value(ResponseErrorType.GLOBAL_ERROR.getName()))
                .andExpect(jsonPath("$[0].message").value("test"))
                .andExpect(jsonPath("$[0].errorCode").exists());

        assertThat(logCaptor.getDebugLogs()).hasSize(1);
    }

    @Test
    void handleMailException() throws Exception {
        when(messages.getMessage(anyString(), any(), any()))
                .thenReturn("test");

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/badMailStub"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$[0].type")
                                   .value(ResponseErrorType.SERVER_ERROR.getName()))
                .andExpect(jsonPath("$[0].fieldName")
                                   .value(ResponseErrorType.SERVER_ERROR.getName()))
                .andExpect(jsonPath("$[0].message").value("test"))
                .andExpect(jsonPath("$[0].errorCode").exists());

        assertThat(logCaptor.getErrorLogs()).hasSize(1);
    }

    @Test
    void handleInternalException() throws Exception {
        when(messages.getMessage(anyString(), any(), any()))
                .thenReturn("test");

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/internalErrorStub"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$[0].type")
                                   .value(ResponseErrorType.SERVER_ERROR.getName()))
                .andExpect(jsonPath("$[0].fieldName")
                                   .value(ResponseErrorType.SERVER_ERROR.getName()))
                .andExpect(jsonPath("$[0].message").value("test"))
                .andExpect(jsonPath("$[0].errorCode").exists());

        assertThat(logCaptor.getErrorLogs()).hasSize(1);
    }

}