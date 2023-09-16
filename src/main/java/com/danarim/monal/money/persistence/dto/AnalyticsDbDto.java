package com.danarim.monal.money.persistence.dto;

import com.danarim.monal.money.persistence.model.TransactionCategory;

/**
 * DTO for analytics. Represents one row from the result of the query in
 * {@link
 * com.danarim.monal.money.persistence.dao.TransactionDao#getTransactionDailyAnalyticsBetweenDates(
 *long, java.util.Date, java.util.Date) TransactionDao#getTransactionDailyAnalyticsBetweenDates}.
 */
public record AnalyticsDbDto(
        String groupedDate,
        TransactionCategory category,
        Double sum
) {

    /**
     * Parses an object to {@link AnalyticsDbDto AnalyticsDbDto}.
     *
     * @param o Object to parse
     *
     * @return {@link AnalyticsDbDto AnalyticsDbDto} parsed from the object
     */
    public static AnalyticsDbDto parse(Object o) {
        Object[] arr = (Object[]) o;
        return new AnalyticsDbDto(
                (String) arr[0],
                (TransactionCategory) arr[1],
                (Double) arr[2]
        );
    }

}
