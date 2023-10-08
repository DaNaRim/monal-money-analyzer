package com.danarim.monal.money.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.auth.AuthUtil;
import com.danarim.monal.failhandler.RestExceptionHandler;
import com.danarim.monal.money.persistence.model.Currency;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.service.WalletService;
import com.danarim.monal.money.web.dto.CreateWalletDto;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.danarim.monal.TestUtils.getExt;
import static com.danarim.monal.TestUtils.postExt;
import static com.danarim.monal.TestUtils.putExt;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WalletController.class)
@ContextConfiguration(classes = {WalletController.class, RestExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class WalletControllerIT {

    private static final MockedStatic<AuthUtil> authUtilMockedStatic = mockStatic(AuthUtil.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @BeforeAll
    static void beforeAll() {
        when(AuthUtil.getLoggedUserId()).thenReturn(1L);
    }

    @AfterAll
    static void afterAll() {
        authUtilMockedStatic.close();
    }

    @Test
    void createWallet() throws Exception {
        CreateWalletDto createWalletDto = new CreateWalletDto("Test", 23.0, "USD");

        Wallet resultWallet = new Wallet(createWalletDto.name(),
                                         createWalletDto.balance(),
                                         Currency.USD,
                                         new User(1L));

        when(walletService.createWallet(createWalletDto, 1L))
                .thenReturn(resultWallet);

        mockMvc.perform(postExt(WebConfig.API_V1_PREFIX + "/wallet", createWalletDto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(createWalletDto.name()))
                .andExpect(jsonPath("$.balance").value(createWalletDto.balance()))
                .andExpect(jsonPath("$.currency").value(createWalletDto.currency()));

        verify(walletService).createWallet(createWalletDto, 1L);
    }

    @Test
    void getUserWallets() throws Exception {
        List<Wallet> wallets = List.of(
                new Wallet("Test", 23.0, Currency.USD, new User(1L)),
                new Wallet("Test2", 42.0, Currency.UAH, new User(1L))
        );
        when(walletService.getUserWallets(1L))
                .thenReturn(wallets);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(wallets.get(0).getName()))
                .andExpect(jsonPath("$[0].balance").value(wallets.get(0).getBalance()))
                .andExpect(jsonPath("$[0].currency").value(wallets.get(0).getCurrency()
                                                                   .toString()))
                .andExpect(jsonPath("$[1].name").value(wallets.get(1).getName()))
                .andExpect(jsonPath("$[1].balance").value(wallets.get(1).getBalance()))
                .andExpect(jsonPath("$[1].currency").value(wallets.get(1).getCurrency()
                                                                   .toString()));
    }

    @Test
    void updateWalletName() throws Exception {
        when(walletService.updateWalletName(1L, "Test", 1L))
                .thenReturn(new Wallet("Test", 23.0, Currency.USD, new User(1L)));

        mockMvc.perform(putExt(WebConfig.API_V1_PREFIX + "/wallet/name")
                                .param("walletId", "1")
                                .param("name", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test"))
                .andExpect(jsonPath("$.balance").value(23.0))
                .andExpect(jsonPath("$.currency").value(Currency.USD.toString()));
    }

    @Test
    void updateWalletName_blankName_BadRequest() throws Exception {
        mockMvc.perform(putExt(WebConfig.API_V1_PREFIX + "/wallet/name")
                                .param("walletId", "1")
                                .param("name", "     "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").exists())
                .andExpect(jsonPath("$[0].errorCode").value("validation.wallet.required.name"))
                .andExpect(jsonPath("$[0].fieldName").value("name"));
    }

    @Test
    void updateWalletName_shortName_BadRequest() throws Exception {
        mockMvc.perform(putExt(WebConfig.API_V1_PREFIX + "/wallet/name")
                                .param("walletId", "1")
                                .param("name", "a"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").exists())
                .andExpect(jsonPath("$[0].errorCode").value("validation.wallet.size.name"))
                .andExpect(jsonPath("$[0].fieldName").value("name"));
    }

    @Test
    void updateWalletName_longName_BadRequest() throws Exception {
        mockMvc.perform(putExt(WebConfig.API_V1_PREFIX + "/wallet/name")
                                .param("walletId", "1")
                                .param("name", "a".repeat(33)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].message").exists())
                .andExpect(jsonPath("$[0].errorCode").value("validation.wallet.size.name"))
                .andExpect(jsonPath("$[0].fieldName").value("name"));
    }

}
