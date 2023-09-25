package com.danarim.monal.money.persistence.dto;

import com.danarim.monal.money.persistence.model.TransactionType;

import java.util.Date;

/**
 * DTO for analytics. Represents one row from the result of the query in
 * {@link
 * com.danarim.monal.money.persistence.dao.TransactionDao#getTransactionAnalyticsBetweenDates(
 * String, long, Date, Date) TransactionDao#getTransactionDailyAnalyticsBetweenDates}
 */
public interface AnalyticsDbDto {

    String getGroupedDate();

    String getCategoryName();

    TransactionType getCategoryType();

    Double getSum();

}
