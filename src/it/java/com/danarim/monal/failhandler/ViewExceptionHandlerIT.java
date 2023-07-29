package com.danarim.monal.failhandler;

import com.danarim.monal.controller.ViewStubController;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import javax.annotation.PostConstruct;

import static com.danarim.monal.TestUtils.getExt;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ViewStubController.class)
@ContextConfiguration(classes = {ViewStubController.class, ViewExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ViewExceptionHandlerIT {

    private static LogCaptor logCaptor;

    @Autowired
    private MockMvc mockMvc;

    @AfterAll
    public static void tearDown() {
        logCaptor.close();
    }

    @AfterEach
    public void clearLogs() {
        logCaptor.clearLogs();
    }

    @PostConstruct
    static void init() {
        logCaptor = LogCaptor.forClass(ViewExceptionHandler.class);
    }

    @Test
    void handleException() throws Exception {
        mockMvc.perform(getExt("/internalErrorStub"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error"));

        assertThat(logCaptor.getErrorLogs())
                .withFailMessage("Expected 1 debug log, but got %d",
                                 logCaptor.getDebugLogs().size())
                .hasSize(1);
    }

}
