package com.danarim.monal.money.web.controller;

import com.danarim.monal.DbUserFiller;
import com.danarim.monal.TestContainersConfig;
import com.danarim.monal.config.WebConfig;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.service.WalletService;
import com.danarim.monal.money.web.dto.CreateWalletDto;
import com.danarim.monal.user.persistence.model.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.danarim.monal.TestUtils.getExtWithAuth;
import static com.danarim.monal.TestUtils.postExtWithAuth;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestContainersConfig.class, DbUserFiller.class})
@ActiveProfiles("test")
class WalletControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Test
    void createWallet() throws Exception {
        CreateWalletDto createWalletDto = new CreateWalletDto("Test", 23.0, "USD");

        Wallet resultWallet = new Wallet(createWalletDto.name(),
                                         createWalletDto.balance(),
                                         "USD",
                                         DbUserFiller.getTestUser());

        when(walletService.createWallet(createWalletDto, DbUserFiller.getTestUserId()))
                .thenReturn(resultWallet);

        mockMvc.perform(postExtWithAuth(WebConfig.API_V1_PREFIX + "/wallet",
                                        createWalletDto,
                                        RoleName.ROLE_USER,
                                        mockMvc))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(createWalletDto.name()))
                .andExpect(jsonPath("$.balance").value(createWalletDto.balance()))
                .andExpect(jsonPath("$.currency").value(createWalletDto.currency()));

        verify(walletService).createWallet(createWalletDto, DbUserFiller.getTestUserId());
    }

    @Test
    void getUserWallets() throws Exception {
        List<Wallet> wallets = List.of(
                new Wallet("Test", 23.0, "USD", DbUserFiller.getTestUser()),
                new Wallet("Test2", 42.0, "UAH", DbUserFiller.getTestUser())
        );

        when(walletService.getUserWallets(DbUserFiller.getTestUserId()))
                .thenReturn(wallets);

        mockMvc.perform(getExtWithAuth(WebConfig.API_V1_PREFIX + "/wallet",
                                       RoleName.ROLE_USER,
                                       mockMvc))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(wallets.get(0).getName()))
                .andExpect(jsonPath("$[0].balance").value(wallets.get(0).getBalance()))
                .andExpect(jsonPath("$[0].currency").value(wallets.get(0).getCurrency()
                                                                   .getCurrencyCode()))
                .andExpect(jsonPath("$[1].name").value(wallets.get(1).getName()))
                .andExpect(jsonPath("$[1].balance").value(wallets.get(1).getBalance()))
                .andExpect(jsonPath("$[1].currency").value(wallets.get(1).getCurrency()
                                                                   .getCurrencyCode()));
    }

}
