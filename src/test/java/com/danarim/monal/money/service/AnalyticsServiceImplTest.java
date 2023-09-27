package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.dao.TransactionDao;
import com.danarim.monal.money.persistence.dto.AnalyticsDbDto;
import com.danarim.monal.money.persistence.model.AnalyticsPeriod;
import com.danarim.monal.money.persistence.model.TransactionType;
import com.danarim.monal.money.web.dto.ViewAnalyticsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    private final TransactionDao transactionDao = mock(TransactionDao.class);
    private final WalletService walletService = mock(WalletService.class);

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    void getAnalytics_Daily() {
        List<AnalyticsDbDto> analyticsDbDtos = getAnalyticsDtosToReturn(AnalyticsPeriod.DAILY);

        when(walletService.isUserWalletOwner(1L, 1L)).thenReturn(true);
        when(transactionDao.getTransactionAnalyticsBetweenDates(
                eq("YYYY-MM-DD"), eq(1L), any(Date.class), any(Date.class))
        ).thenReturn(analyticsDbDtos);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.JANUARY, 6);

        ViewAnalyticsDto result =
                analyticsService.getAnalytics(AnalyticsPeriod.DAILY, 1L, calendar.getTime(), 1L);

        Map<String, Map<String, Double>> expectedOutcome = Map.of(
                "2021-01-01", Map.of(
                        "FOOD", 100.0,
                        "HEALTH", 100.0
                ),
                "2021-01-02", Map.of("FOOD", 100.0)
        );
        Map<String, Map<String, Double>> expectedIncome = Map.of(
                "2021-01-02", Map.of("SALARY", 100.0)
        );

        assertEquals(expectedOutcome, result.outcome());
        assertEquals(expectedIncome, result.income());

        verify(walletService).isUserWalletOwner(1L, 1L);
        verify(transactionDao).getTransactionAnalyticsBetweenDates(
                eq("YYYY-MM-DD"), eq(1L), any(Date.class), any(Date.class)
        );

        calendar.set(2021, Calendar.JANUARY, 1);
        Date expectedFrom = calendar.getTime();
        calendar.set(2021, Calendar.JANUARY, 31);
        Date expectedTo = calendar.getTime();

        verify(transactionDao).getTransactionAnalyticsBetweenDates(
                eq("YYYY-MM-DD"), eq(1L), eq(expectedFrom), eq(expectedTo)
        );
    }

    @Test
    void getAnalytics_Monthly() {
        List<AnalyticsDbDto> analyticsDbDtos = getAnalyticsDtosToReturn(AnalyticsPeriod.MONTHLY);

        when(walletService.isUserWalletOwner(1L, 1L)).thenReturn(true);
        when(transactionDao.getTransactionAnalyticsBetweenDates(
                eq("YYYY-MM"), eq(1L), any(Date.class), any(Date.class))
        ).thenReturn(analyticsDbDtos);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.JANUARY, 6);

        ViewAnalyticsDto result =
                analyticsService.getAnalytics(AnalyticsPeriod.MONTHLY, 1L, calendar.getTime(), 1L);

        Map<String, Map<String, Double>> expectedOutcome = Map.of(
                "2021-01", Map.of(
                        "FOOD", 100.0,
                        "HEALTH", 100.0
                ),
                "2021-02", Map.of("FOOD", 100.0)
        );
        Map<String, Map<String, Double>> expectedIncome = Map.of(
                "2021-02", Map.of("SALARY", 100.0)
        );

        assertEquals(expectedOutcome, result.outcome());
        assertEquals(expectedIncome, result.income());

        verify(walletService).isUserWalletOwner(1L, 1L);
        verify(transactionDao).getTransactionAnalyticsBetweenDates(
                eq("YYYY-MM"), eq(1L), any(Date.class), any(Date.class)
        );

        calendar.set(2021, Calendar.JANUARY, 1);
        Date expectedFrom = calendar.getTime();
        calendar.set(2021, Calendar.DECEMBER, 31);
        Date expectedTo = calendar.getTime();

        verify(transactionDao).getTransactionAnalyticsBetweenDates(
                eq("YYYY-MM"), eq(1L), eq(expectedFrom), eq(expectedTo)
        );
    }

    @Test
    void getAnalytics_Yearly() {
        List<AnalyticsDbDto> analyticsDbDtos = getAnalyticsDtosToReturn(AnalyticsPeriod.YEARLY);

        when(walletService.isUserWalletOwner(1L, 1L)).thenReturn(true);
        when(transactionDao.getTransactionAnalyticsBetweenDates(
                eq("YYYY"), eq(1L), any(Date.class), any(Date.class))
        ).thenReturn(analyticsDbDtos);

        Calendar calendar = Calendar.getInstance();
        calendar.set(2021, Calendar.JANUARY, 6);

        ViewAnalyticsDto result =
                analyticsService.getAnalytics(AnalyticsPeriod.YEARLY, 1L, calendar.getTime(), 1L);

        Map<String, Map<String, Double>> expectedOutcome = Map.of(
                "2021", Map.of(
                        "FOOD", 100.0,
                        "HEALTH", 100.0
                ),
                "2022", Map.of("FOOD", 100.0)
        );
        Map<String, Map<String, Double>> expectedIncome = Map.of(
                "2022", Map.of("SALARY", 100.0)
        );

        assertEquals(expectedOutcome, result.outcome());
        assertEquals(expectedIncome, result.income());

        verify(walletService).isUserWalletOwner(1L, 1L);
        verify(transactionDao).getTransactionAnalyticsBetweenDates(
                eq("YYYY"), eq(1L), any(Date.class), any(Date.class)
        );

        calendar.set(2020, Calendar.JANUARY, 1); // One year before
        Date expectedFrom = calendar.getTime();
        calendar.set(2022, Calendar.DECEMBER, 31); // One year after
        Date expectedTo = calendar.getTime();

        verify(transactionDao).getTransactionAnalyticsBetweenDates(
                eq("YYYY"), eq(1L), eq(expectedFrom), eq(expectedTo)
        );
    }

    @Test
    void getAnalytics_useNotWalletOwner_AccessDeniedException() {
        when(walletService.isUserWalletOwner(1L, 1L)).thenReturn(false);

        assertThrows(
                AccessDeniedException.class,
                () -> analyticsService.getAnalytics(AnalyticsPeriod.DAILY, 1L, new Date(), 1L)
        );
    }

    private static List<AnalyticsDbDto> getAnalyticsDtosToReturn(AnalyticsPeriod period) {
        String[] dates = switch (period) {
            case DAILY -> new String[] {"2021-01-01", "2021-01-02"};
            case MONTHLY -> new String[] {"2021-01", "2021-02"};
            case YEARLY -> new String[] {"2021", "2022"};
        };
        AnalyticsDbDto analyticsRow1 = mock(AnalyticsDbDto.class);
        when(analyticsRow1.getGroupedDate()).thenReturn(dates[0]);
        when(analyticsRow1.getCategoryName()).thenReturn("FOOD");
        when(analyticsRow1.getCategoryType()).thenReturn(TransactionType.OUTCOME);
        when(analyticsRow1.getSum()).thenReturn(100.0);

        AnalyticsDbDto analyticsRow2 = mock(AnalyticsDbDto.class);
        when(analyticsRow2.getGroupedDate()).thenReturn(dates[0]);
        when(analyticsRow2.getCategoryName()).thenReturn("HEALTH");
        when(analyticsRow2.getCategoryType()).thenReturn(TransactionType.OUTCOME);
        when(analyticsRow2.getSum()).thenReturn(100.0);

        AnalyticsDbDto analyticsRow3 = mock(AnalyticsDbDto.class);
        when(analyticsRow3.getGroupedDate()).thenReturn(dates[1]);
        when(analyticsRow3.getCategoryName()).thenReturn("FOOD");
        when(analyticsRow3.getCategoryType()).thenReturn(TransactionType.OUTCOME);
        when(analyticsRow3.getSum()).thenReturn(100.0);

        AnalyticsDbDto analyticsRow4 = mock(AnalyticsDbDto.class);
        when(analyticsRow4.getGroupedDate()).thenReturn(dates[1]);
        when(analyticsRow4.getCategoryName()).thenReturn("SALARY");
        when(analyticsRow4.getCategoryType()).thenReturn(TransactionType.INCOME);
        when(analyticsRow4.getSum()).thenReturn(100.0);

        return List.of(
                analyticsRow1,
                analyticsRow2,
                analyticsRow3,
                analyticsRow4
        );
    }

}
