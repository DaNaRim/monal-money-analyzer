package com.danarim.monal.failhandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.danarim.monal.TestUtils.getExt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = NotFoundExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class NotFoundExceptionHandlerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void handleNoHandlerFoundException_latinCharacters() throws Exception {
        mockMvc.perform(getExt("/asd"))
                .andExpect(status().isNotFound())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
    void handleNoHandlerFoundException_notLatinCharacters() throws Exception {
        mockMvc.perform(getExt("/\uFFFD\uFFFD\uFFFD\uFFFD"))
                .andExpect(status().isNotFound())
                .andExpect(forwardedUrl("/index.html"));
    }

}
