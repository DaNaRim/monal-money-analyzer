package com.danarim.monal.failhandler;

import com.danarim.monal.controller.ViewStubController;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.danarim.monal.TestUtils.getExt;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ViewStubController.class)
@ContextConfiguration(classes = {ViewStubController.class, ViewExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class ViewExceptionHandlerIT {

    private static LogCaptor logCaptor;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        //Lazy init because of logcaptor is created before the test application context is created
        if (logCaptor == null) {
            logCaptor = LogCaptor.forClass(ViewExceptionHandler.class);
        }
    }

    @AfterAll
    public static void tearDown() {
        logCaptor.close();
    }

    @AfterEach
    public void clearLogs() {
        logCaptor.clearLogs();
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
