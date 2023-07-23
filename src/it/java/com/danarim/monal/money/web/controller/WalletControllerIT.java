package com.danarim.monal.money.web.controller;

import com.danarim.monal.DbUserFiller;
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
import org.springframework.test.web.servlet.MockMvc;

import static com.danarim.monal.TestUtils.postExtWithAuth;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(DbUserFiller.class)
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

}
