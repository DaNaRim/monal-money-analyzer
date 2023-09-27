package com.danarim.monal.money.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.auth.AuthUtil;
import com.danarim.monal.money.persistence.model.AnalyticsPeriod;
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

    /**
     * Gets analytics for a wallet for a date and period.
     *
     * @param walletId wallet ID to get analytics for
     * @param date     date to get analytics for in format yyyy-MM
     * @param period   period of time to get analytics for {@link AnalyticsPeriod AnalyticsPeriod}
     *
     * @return {@link ViewAnalyticsDto ViewAnalyticsDto} with analytics data
     */
    @GetMapping
    public ViewAnalyticsDto getAnalytics(
            @RequestParam("walletId") Long walletId,
            @RequestParam("date") @DateTimeFormat(fallbackPatterns = {"yyyy-MM", "yyyy"}) Date date,
            @RequestParam("period") AnalyticsPeriod period
    ) {
        return analyticsService.getAnalytics(period,
                                             walletId,
                                             date,
                                             AuthUtil.getLoggedUserId());
    }

}
