package com.danarim.monal.money.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.auth.AuthUtil;
import com.danarim.monal.money.service.AnalyticsService;
import com.danarim.monal.money.web.dto.ViewAnalyticsDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Controller for transactions analytics.
 */
@RestController
@RequestMapping(WebConfig.API_V1_PREFIX + "/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/daily")
    public ViewAnalyticsDto getDailyAnalytics(
            @RequestParam("walletId") Long walletId,
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM") Date date
    ) {
        return analyticsService.getDailyAnalytics(walletId, date, AuthUtil.getLoggedUserId());
    }

}
