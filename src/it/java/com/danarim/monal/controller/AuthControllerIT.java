package com.danarim.monal.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ContextConfiguration(classes = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(post(WebConfig.API_V1_PREFIX + "/logout"))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists(JwtUtil.KEY_ACCESS_TOKEN))
                .andExpect(cookie().httpOnly(JwtUtil.KEY_ACCESS_TOKEN, true))
                .andExpect(cookie().maxAge(JwtUtil.KEY_ACCESS_TOKEN, 0))
                .andExpect(cookie().exists(JwtUtil.KEY_REFRESH_TOKEN))
                .andExpect(cookie().httpOnly(JwtUtil.KEY_REFRESH_TOKEN, true))
                .andExpect(cookie().maxAge(JwtUtil.KEY_REFRESH_TOKEN, 0));
    }

}
