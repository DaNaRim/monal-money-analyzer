package com.danarim.monal.money.web.controller;

import com.danarim.monal.DbUserFiller;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.service.TransactionService;
import com.danarim.monal.money.web.dto.CreateTransactionDto;
import com.danarim.monal.user.persistence.model.RoleName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.danarim.monal.TestUtils.getExtWithAuth;
import static com.danarim.monal.TestUtils.postExtWithAuth;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbUserFiller.class)
class TransactionControllerIT {

    private static final SimpleDateFormat dateFormatter =
            new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @BeforeAll
    static void beforeAll() {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Test
    void createTransaction() throws Exception {
        CreateTransactionDto dto = new CreateTransactionDto(
                "test", new Date(), 1.0, 1L, 1L
        );
        when(transactionService.createTransaction(any(CreateTransactionDto.class),
                                                  eq(DbUserFiller.getTestUserId())))
                .thenAnswer(invocation -> {
                    Transaction transaction = new Transaction(
                            dto.description(),
                            dto.date(),
                            dto.amount(),
                            new TransactionCategory(dto.categoryId()),
                            new Wallet("Test", 0.0, "USD", DbUserFiller.getTestUser())
                    );
                    transaction.setId(1L);

                    return transaction;
                });
        mockMvc.perform(postExtWithAuth(WebConfig.API_V1_PREFIX + "/transaction",
                                        dto,
                                        RoleName.ROLE_USER,
                                        mockMvc))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value(dto.description()))
                .andExpect(jsonPath("$.date").value(dateFormatter.format(dto.date())))
                .andExpect(jsonPath("$.amount").value(dto.amount()))
                .andExpect(jsonPath("$.categoryId").value(dto.categoryId()))
                .andExpect(jsonPath("$.walletId").doesNotExist());
    }

    @Test
    void getTransactionBetweenDates() throws Exception {
        List<Transaction> transactions = prepareTransaction();

        when(transactionService.getTransactionsBetweenDates(any(Date.class),
                                                            any(Date.class),
                                                            eq(1L),
                                                            eq(DbUserFiller.getTestUserId())))
                .thenReturn(transactions);

        mockMvc.perform(getExtWithAuth(WebConfig.API_V1_PREFIX + "/transaction/date",
                                       RoleName.ROLE_USER,
                                       mockMvc)
                                .param("from", "2020-01-01 00")
                                .param("to", "2020-01-02 00")
                                .param("walletId", "1"))
                .andExpect(status().isOk())
                // Transaction with the newest date should be first
                .andExpect(jsonPath("$.length()").value(transactions.size()))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].description").value(transactions.get(1).getDescription()))
                .andExpect(jsonPath("$[0].date")
                                   .value(dateFormatter.format(transactions.get(1).getDate())))
                .andExpect(jsonPath("$[0].amount").value(transactions.get(1).getAmount()))
                .andExpect(jsonPath("$[0].categoryId")
                                   .value(transactions.get(1).getCategory().getId()))
                .andExpect(jsonPath("$[0].walletId").doesNotExist());
    }

    private static List<Transaction> prepareTransaction() throws ParseException {
        Wallet wallet = new Wallet("Test", 0.0, "USD", DbUserFiller.getTestUser());
        wallet.setId(1L);

        List<Transaction> transactions = List.of(
                new Transaction(
                        "test",
                        new Date(dateFormatter.parse("2020-01-01 12:40:00").getTime()),
                        1.0,
                        new TransactionCategory(1L),
                        wallet
                ),
                new Transaction(
                        "test2",
                        new Date(dateFormatter.parse("2020-01-01 12:41:00").getTime()),
                        1.0,
                        new TransactionCategory(1L),
                        wallet
                )
        );
        return transactions;
    }

}
