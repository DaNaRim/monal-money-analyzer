package com.danarim.monal.money.persistence.model;

/**
 * Represents the period of time for which analytics are calculated.
 */
public enum AnalyticsPeriod {
    DAILY("YYYY-MM-DD"),
    MONTHLY("YYYY-MM"),
    YEARLY("YYYY");

    private final String dateFormat;

    AnalyticsPeriod(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDateFormat() {
        return dateFormat;
    }
}
