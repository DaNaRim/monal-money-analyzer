package com.danarim.monal;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@TestConfiguration
public class TestConfig {

    @Bean
    public MockMvc mockMvc(WebApplicationContext context) {
        return MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .defaultRequest(get("/").secure(true))
                .defaultRequest(post("/").secure(true))
                .defaultRequest(put("/").secure(true))
                .defaultRequest(delete("/").secure(true))
                .build();
    }
}
