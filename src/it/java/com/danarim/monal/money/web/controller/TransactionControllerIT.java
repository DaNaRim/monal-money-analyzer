package com.danarim.monal.money.web.controller;

import com.danarim.monal.DbUserFiller;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.service.TransactionService;
import com.danarim.monal.money.web.dto.CreateTransactionDto;
import com.danarim.monal.user.persistence.model.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static com.danarim.monal.TestUtils.postExtWithAuth;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbUserFiller.class)
class TransactionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createTransaction() throws Exception {
        CreateTransactionDto dto = new CreateTransactionDto(
                "test", new Date(), 1.0, 1L, 1L
        );
        when(transactionService.createTransaction(dto, DbUserFiller.getTestUserId()))
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
                .andExpect(jsonPath("$.date").value(dto.date().getTime()))
                .andExpect(jsonPath("$.amount").value(dto.amount()))
                .andExpect(jsonPath("$.categoryId").value(dto.categoryId()))
                .andExpect(jsonPath("$.walletId").doesNotExist());
    }

}
