package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.model.AnalyticsPeriod;
import com.danarim.monal.money.web.dto.ViewAnalyticsDto;

import java.util.Date;

/**
 * Service for transactions analytics.
 */
public interface AnalyticsService {

    ViewAnalyticsDto getAnalytics(AnalyticsPeriod period, Long walletId, Date date, long userId);

}
