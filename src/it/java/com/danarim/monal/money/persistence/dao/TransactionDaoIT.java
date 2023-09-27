package com.danarim.monal.money.persistence.dao;

import com.danarim.monal.DbUserFiller;
import com.danarim.monal.TestContainersConfig;
import com.danarim.monal.money.persistence.dto.AnalyticsDbDto;
import com.danarim.monal.money.persistence.model.AnalyticsPeriod;
import com.danarim.monal.money.persistence.model.Currency;
import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.TransactionType;
import com.danarim.monal.money.persistence.model.Wallet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({TestContainersConfig.class, DbUserFiller.class})
@ActiveProfiles("test")
class TransactionDaoIT {

    @Autowired
    private TransactionDao transactionDao;

    @Autowired
    private WalletDao walletDao;

    @Autowired
    private TransactionCategoryDao transactionCategoryDao;

    @BeforeAll
    static void beforeAll() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void getTransactionAnalyticsBetweenDates_Daily() {
        fillDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prepareDate(2021, 1, 1));
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        Date from = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Date to = calendar.getTime();

        List<AnalyticsDbDto> result = transactionDao.getTransactionAnalyticsBetweenDates(
                AnalyticsPeriod.DAILY.getDateFormat(), 1L, from, to
        );
        assertEquals(3, result.size(), "Wrong number of analytics returned");

        assertEquals("2021-01-01", result.get(0).getGroupedDate());
        assertEquals("Category 1", result.get(0).getCategoryName());
        assertEquals(TransactionType.OUTCOME, result.get(0).getCategoryType());
        assertEquals(2.0, result.get(0).getSum());

        assertEquals("2021-01-01", result.get(1).getGroupedDate());
        assertEquals("Category 2", result.get(1).getCategoryName());
        assertEquals(TransactionType.OUTCOME, result.get(0).getCategoryType());
        assertEquals(1.0, result.get(1).getSum());

        assertEquals("2021-01-02", result.get(2).getGroupedDate());
        assertEquals("Category 1", result.get(2).getCategoryName());
        assertEquals(1.0, result.get(2).getSum());
    }

    @Test
    void getTransactionAnalyticsBetweenDates_Monthly() {
        fillDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prepareDate(2021, 1, 1));
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        Date from = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        Date to = calendar.getTime();

        List<AnalyticsDbDto> result = transactionDao.getTransactionAnalyticsBetweenDates(
                AnalyticsPeriod.MONTHLY.getDateFormat(), 1L, from, to
        );
        assertEquals(3, result.size(), "Wrong number of analytics returned");

        assertEquals("2021-01", result.get(0).getGroupedDate());
        assertEquals("Category 1", result.get(0).getCategoryName());
        assertEquals(TransactionType.OUTCOME, result.get(0).getCategoryType());
        assertEquals(3.0, result.get(0).getSum());

        assertEquals("2021-01", result.get(1).getGroupedDate());
        assertEquals("Category 2", result.get(1).getCategoryName());
        assertEquals(TransactionType.OUTCOME, result.get(0).getCategoryType());
        assertEquals(1.0, result.get(1).getSum());

        assertEquals("2021-02", result.get(2).getGroupedDate());
        assertEquals("Category 2", result.get(2).getCategoryName());
        assertEquals(TransactionType.OUTCOME, result.get(0).getCategoryType());
        assertEquals(1.0, result.get(2).getSum());
    }

    @Test
    void getTransactionAnalyticsBetweenDates_Yearly() {
        fillDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prepareDate(2021, 1, 1));
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        Date from = calendar.getTime();
        calendar.add(Calendar.YEAR, 1);
        calendar.add(Calendar.MONTH, 1);
        Date to = calendar.getTime();

        List<AnalyticsDbDto> result = transactionDao.getTransactionAnalyticsBetweenDates(
                AnalyticsPeriod.YEARLY.getDateFormat(), 1L, from, to
        );
        assertEquals(3, result.size(), "Wrong number of analytics returned");

        assertEquals("2021", result.get(0).getGroupedDate());
        assertEquals("Category 1", result.get(0).getCategoryName());
        assertEquals(TransactionType.OUTCOME, result.get(0).getCategoryType());
        assertEquals(3.0, result.get(0).getSum());

        assertEquals("2021", result.get(1).getGroupedDate());
        assertEquals("Category 2", result.get(1).getCategoryName());
        assertEquals(TransactionType.OUTCOME, result.get(0).getCategoryType());
        assertEquals(2.0, result.get(1).getSum());

        assertEquals("2022", result.get(2).getGroupedDate());
        assertEquals("Category 2", result.get(2).getCategoryName());
        assertEquals(TransactionType.OUTCOME, result.get(0).getCategoryType());
        assertEquals(1.0, result.get(2).getSum());
    }

    private static Date prepareDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private void fillDatabase() {
        if (walletDao.existsById(1L)) {
            return;
        }

        Wallet wallet = new Wallet("Test", 1.0, Currency.USD, DbUserFiller.getTestUser());
        walletDao.save(wallet);

        TransactionCategory category1 =
                new TransactionCategory("Category 1", TransactionType.OUTCOME, null);
        TransactionCategory category2 =
                new TransactionCategory("Category 2", TransactionType.OUTCOME, null);

        transactionCategoryDao.save(category1);
        transactionCategoryDao.save(category2);

        Transaction transaction1
                = new Transaction("Test", prepareDate(2021, 1, 1), 1.0, category1, wallet);
        Transaction transaction2
                = new Transaction("Test", prepareDate(2021, 1, 1), 1.0, category1, wallet);
        Transaction transaction3
                = new Transaction("Test", prepareDate(2021, 1, 2), 1.0, category1, wallet);
        Transaction transaction4
                = new Transaction("Test", prepareDate(2021, 1, 1), 1.0, category2, wallet);
        Transaction transaction5
                = new Transaction("Test", prepareDate(2021, 2, 1), 1.0, category2, wallet);
        Transaction transaction6
                = new Transaction("Test", prepareDate(2022, 2, 1), 1.0, category2, wallet);

        transactionDao.save(transaction1);
        transactionDao.save(transaction2);
        transactionDao.save(transaction3);
        transactionDao.save(transaction4);
        transactionDao.save(transaction5);
        transactionDao.save(transaction6);
    }

}
