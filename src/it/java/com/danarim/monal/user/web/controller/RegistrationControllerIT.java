package com.danarim.monal.user.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@ContextConfiguration(classes = RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class RegistrationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @Test
    void testRegisterUser() throws Exception {
        RegistrationDto registrationDto = new RegistrationDto(
                "John", "Doe",
                "test1234", "test1234",
                "test@test.test"
        );

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

        mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/registration")
                        .contentType(APPLICATION_JSON)
                        .content(ow.writeValueAsString(registrationDto))
                )
                .andExpect(status().isCreated());

        verify(registrationService).registerNewUserAccount(registrationDto);
    }

    @Test
    void testRegisterUserInvalidData() throws Exception {
        RegistrationDto registrationDto = new RegistrationDto(
                "John", "Doe",
                "test1234", "test1234",
                "invalid"
        );

        final ObjectMapper mapper = new ObjectMapper();
        final ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

        mockMvc.perform(post(WebConfig.BACKEND_PREFIX + "/registration")
                        .contentType(APPLICATION_JSON)
                        .content(ow.writeValueAsString(registrationDto))
                )
                .andExpect(status().isBadRequest());

        verify(registrationService, never()).registerNewUserAccount(registrationDto);
    }
}
