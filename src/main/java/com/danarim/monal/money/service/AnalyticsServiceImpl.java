package com.danarim.monal.money.service;

import com.danarim.monal.exceptions.InternalServerException;
import com.danarim.monal.money.persistence.dao.TransactionDao;
import com.danarim.monal.money.persistence.dto.AnalyticsDbDto;
import com.danarim.monal.money.persistence.model.AnalyticsPeriod;
import com.danarim.monal.money.persistence.model.TransactionType;
import com.danarim.monal.money.web.dto.ViewAnalyticsDto;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for analytics reports.
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private final TransactionDao transactionDao;
    private final WalletService walletService;

    public AnalyticsServiceImpl(TransactionDao transactionDao, WalletService walletService) {
        this.transactionDao = transactionDao;
        this.walletService = walletService;
    }

    /**
     * Gets analytics for a wallet for a specific month.
     *
     * @param walletId     wallet ID to get analytics for
     * @param date         date to get analytics for (month and year)
     * @param loggedUserId logged in user ID
     *
     * @return {@link ViewAnalyticsDto ViewAnalyticsDto} with analytics data
     *
     * @throws AccessDeniedException if the user is not the owner of the wallet
     */
    @Override
    public ViewAnalyticsDto getDailyAnalytics(Long walletId,
                                              Date date,
                                              long loggedUserId
    ) {
        if (!walletService.isUserWalletOwner(walletId, loggedUserId)) {
            throw new AccessDeniedException("User with ID %d is not the owner of wallet with ID %d"
                                                    .formatted(loggedUserId, walletId));
        }
        List<AnalyticsDbDto> analytics = transactionDao.getTransactionAnalyticsBetweenDates(
                AnalyticsPeriod.DAILY.getDateFormat(),
                walletId,
                getStartDateByPeriod(AnalyticsPeriod.DAILY, date),
                getEndDateByPeriod(AnalyticsPeriod.DAILY, date)
        );
        return new ViewAnalyticsDto(
                convertAnalyticsWithTypeFilter(analytics, TransactionType.INCOME),
                convertAnalyticsWithTypeFilter(analytics, TransactionType.OUTCOME)
        );
    }

    /**
     * Get start date for the given period and date. For example, if the period is
     * {@link AnalyticsPeriod#DAILY DAILY} and the date is 2021-01-15, the start date will be
     * 2021-01-01.
     *
     * @param period analytics period to get the start date for
     * @param date   date to get the start date for
     *
     * @return start date for the given period and date
     */
    private static Date getStartDateByPeriod(AnalyticsPeriod period, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        switch (period) {
            case DAILY -> calendar.set(Calendar.DAY_OF_MONTH, 1);
            case MONTHLY -> {
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            }
            case YEARLY -> {
                calendar.add(Calendar.YEAR, -3);
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            }
            default -> throw new InternalServerException(
                    "Unsupported analytics period: %s".formatted(period)
            );
        }
        return calendar.getTime();
    }

    /**
     * Get end date for the given period and date. For example, if the period is
     * {@link AnalyticsPeriod#DAILY DAILY} and the date is 2021-01-15, the end date will be
     * 2021-01-31.
     *
     * @param period analytics period to get the end date for
     * @param date   date to get the end date for
     *
     * @return end date for the given period and date
     */
    private static Date getEndDateByPeriod(AnalyticsPeriod period, Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        switch (period) {
            case DAILY -> calendar.set(Calendar.DAY_OF_MONTH,
                                       calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            case MONTHLY -> {
                calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH,
                             calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            case YEARLY -> {
                calendar.add(Calendar.YEAR, 3);
                calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH,
                             calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            }
            default -> throw new InternalServerException(
                    "Unsupported analytics period: %s".formatted(period)
            );
        }
        return calendar.getTime();
    }

    private static Map<String, Map<String, Double>> convertAnalyticsWithTypeFilter(
            List<AnalyticsDbDto> analytics,
            TransactionType type
    ) {
        return analytics.stream()
                .filter(dto -> dto.getCategoryType() == type)
                .map(dto -> Map.entry(dto.getGroupedDate(),
                                      Map.entry(dto.getCategoryName(), dto.getSum())))
                .collect(Collectors.toMap(
                        // Key mapper
                        Map.Entry::getKey,
                        // Value mapper
                        entry -> {
                            Map<String, Double> innerMap = new HashMap<>();
                            innerMap.put(entry.getValue().getKey(), entry.getValue().getValue());
                            return innerMap;
                        },
                        // Merge function
                        (existing, replacement) -> {
                            existing.putAll(replacement);
                            return existing;
                        }
                ));
    }

}
