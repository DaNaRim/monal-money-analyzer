package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.dao.TransactionDao;
import com.danarim.monal.money.persistence.dto.AnalyticsDbDto;
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
        List<Object> unparsedAnalytics = transactionDao.getTransactionDailyAnalyticsBetweenDates(
                walletId,
                getMonthStart(date),
                getMonthEnd(date)
        );
        List<AnalyticsDbDto> parsedAnalytics = unparsedAnalytics.stream()
                .map(AnalyticsDbDto::parse)
                .toList();

        return new ViewAnalyticsDto(
                convertAnalyticsWithTypeFilter(parsedAnalytics, TransactionType.INCOME),
                convertAnalyticsWithTypeFilter(parsedAnalytics, TransactionType.OUTCOME)
        );
    }

    /**
     * Calculates the start of the month for a given date.
     *
     * @param date date to get the start of the month for
     *
     * @return date with the start of the month
     */
    private static Date getMonthStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // We should keep hours to get the correct date in the UTC time zone
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    /**
     * Calculates the end of the month for a given date.
     *
     * @param date date to get the end of the month for
     *
     * @return date with the end of the month
     */
    private static Date getMonthEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        // We should keep hours to get the correct date in the UTC time zone
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return calendar.getTime();
    }

    private static Map<String, Map<String, Double>> convertAnalyticsWithTypeFilter(
            List<AnalyticsDbDto> analytics,
            TransactionType type
    ) {
        return analytics.stream()
                .filter(dto -> dto.category().getType() == type)
                .map(dto -> Map.entry(dto.groupedDate(),
                                      Map.entry(dto.category().getName(), dto.sum())))
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
