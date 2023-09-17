package com.danarim.monal.money.web.dto;

import java.util.Map;

/**
 * Represents the analytics for a wallet.
 *
 * <p>Map [date in string format, Map [category (use category name), sum of
 * amounts for the category]]
 *
 * @param income  Map for income analytics
 * @param outcome Map for outcome analytics
 */
public record ViewAnalyticsDto(
        Map<String, Map<String, Double>> income,
        Map<String, Map<String, Double>> outcome
) {

}
