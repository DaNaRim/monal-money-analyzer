package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.money.persistence.dto.AnalyticsDbDto;
import com.danarim.monal.money.persistence.model.AnalyticsPeriod;
import com.danarim.monal.money.persistence.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * DAO for {@link Transaction Transaction} and analytics.
 */
public interface TransactionDao extends JpaRepository<Transaction, Long> {

    List<Transaction> getTransactionsByWalletIdAndDateBetween(long walletId, Date from, Date to);

    /**
     * Used to get analytics for a wallet for a specific period. The result is grouped by date and
     * category.
     *
     * @param analyticsDateFormat date format to group by. Use {@link AnalyticsPeriod#getDateFormat}
     *                            to get the correct format for the period.
     * @param walletId            wallet ID to get analytics for
     * @param from                start date
     * @param to                  end date
     *
     * @return {@link AnalyticsDbDto} analytics data (grouped by date, grouped by category name and
     *         type, sum of amounts)
     */
    @Query(
            value = """
                    SELECT TO_CHAR(t.date, :analyticsDateFormat) AS groupeddate,
                           c.name AS categoryname,
                           c.type AS categorytype,
                           SUM(t.amount) AS sum
                      FROM transaction AS t
                           INNER JOIN transaction_category AS c ON c.id = t.category_id
                     WHERE t.wallet_id = :walletId AND t.date BETWEEN :from AND :to
                     GROUP BY groupeddate, categoryname, categorytype
                       """,
            nativeQuery = true
    )
    List<AnalyticsDbDto> getTransactionAnalyticsBetweenDates(
            String analyticsDateFormat,
            long walletId,
            Date from,
            Date to
    );

}
