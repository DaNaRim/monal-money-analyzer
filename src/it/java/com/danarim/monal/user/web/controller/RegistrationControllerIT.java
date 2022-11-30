package com.danarim.monal.user.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.failHandler.GlobalExceptionHandler;
import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.web.dto.RegistrationDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static com.danarim.monal.TestUtils.postExt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@ContextConfiguration(classes = {RegistrationController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
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

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
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

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/registration", registrationDto))
                .andExpect(status().isBadRequest())

                .andExpect(jsonPath("$[0].type").value("Pattern"))
                .andExpect(jsonPath("$[0].fieldName").value("email"))
                .andExpect(jsonPath("$[0].message").exists());

        verify(registrationService, never()).registerNewUserAccount(registrationDto);
    }
}
