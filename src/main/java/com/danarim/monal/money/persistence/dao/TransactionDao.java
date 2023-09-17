package com.danarim.monal.money.persistence.dao;

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
     * Used to get analytics for a wallet for a specific month. Can be parsed to
     * {@link com.danarim.monal.money.persistence.dto.AnalyticsDbDto AnalyticsDbDto}.
     *
     * @param walletId wallet ID to get analytics for
     * @param from     start date
     * @param to       end date
     *
     * @return analytics data (grouped by date, grouped by category, sum of amounts for the
     *     category)
     */
    @Query(
            """
                    SELECT TO_CHAR(t.date, 'YYYY-MM-DD') AS grouped_date, t.category, SUM(t.amount)
                      FROM Transaction t
                           INNER JOIN t.category transactio1_ ON t.category.id = transactio1_.id
                     WHERE t.wallet.id = :walletId AND t.date BETWEEN :from AND :to
                     GROUP BY grouped_date, t.category, transactio1_.id
                       """
    )
    List<Object> getTransactionDailyAnalyticsBetweenDates(
            long walletId,
            Date from,
            Date to
    );

}
