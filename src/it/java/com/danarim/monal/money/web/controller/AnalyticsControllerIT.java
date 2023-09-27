package com.danarim.monal.money.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.auth.AuthUtil;
import com.danarim.monal.failhandler.RestExceptionHandler;
import com.danarim.monal.money.persistence.model.AnalyticsPeriod;
import com.danarim.monal.money.service.AnalyticsService;
import com.danarim.monal.money.web.dto.ViewAnalyticsDto;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.Map;

import static com.danarim.monal.TestUtils.getExt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@ContextConfiguration(classes = {AnalyticsController.class, RestExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AnalyticsControllerIT {

    private static final MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class);

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AnalyticsService analyticsService;

    @BeforeAll
    static void beforeAll() {
        when(AuthUtil.getLoggedUserId()).thenReturn(1L);
    }

    @AfterAll
    static void afterAll() {
        authUtilMockedStatic.close();
    }

    @Test
    void getAnalytics_Daily() throws Exception {
        ViewAnalyticsDto viewAnalyticsDto = new ViewAnalyticsDto(
                Map.of("2021-01-01", Map.of("Test", 1.0)),
                Map.of("2021-01-01", Map.of("Test", 1.0))
        );
        when(analyticsService.getAnalytics(eq(AnalyticsPeriod.DAILY),
                                           eq(1L),
                                           any(Date.class),
                                           eq(1L)))
                .thenReturn(viewAnalyticsDto);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/analytics")
                                .param("walletId", "1")
                                .param("date", "2021-01-01")
                                .param("period", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income['2021-01-01'].Test").value(1.0))
                .andExpect(jsonPath("$.outcome['2021-01-01'].Test").value(1.0));
    }

    @Test
    void getAnalytics_Monthly() throws Exception {
        ViewAnalyticsDto viewAnalyticsDto = new ViewAnalyticsDto(
                Map.of("2021-01", Map.of("Test", 1.0)),
                Map.of("2021-01", Map.of("Test", 1.0))
        );
        when(analyticsService.getAnalytics(eq(AnalyticsPeriod.MONTHLY),
                                           eq(1L),
                                           any(Date.class),
                                           eq(1L)))
                .thenReturn(viewAnalyticsDto);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/analytics")
                                .param("walletId", "1")
                                .param("date", "2021-05") // month should be ignored
                                .param("period", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income['2021-01'].Test").value(1.0))
                .andExpect(jsonPath("$.outcome['2021-01'].Test").value(1.0));
    }

    @Test
    void getAnalytics_Yearly() throws Exception {
        ViewAnalyticsDto viewAnalyticsDto = new ViewAnalyticsDto(
                Map.of("2021", Map.of("Test", 1.0)),
                Map.of("2021", Map.of("Test", 1.0))
        );
        when(analyticsService.getAnalytics(eq(AnalyticsPeriod.YEARLY),
                                           eq(1L),
                                           any(Date.class),
                                           eq(1L)))
                .thenReturn(viewAnalyticsDto);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/analytics")
                                .param("walletId", "1")
                                .param("date", "2021")
                                .param("period", "YEARLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income['2021'].Test").value(1.0))
                .andExpect(jsonPath("$.outcome['2021'].Test").value(1.0));
    }

}
