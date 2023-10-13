package com.danarim.monal.money.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.auth.AuthUtil;
import com.danarim.monal.money.persistence.model.Wallet;
import com.danarim.monal.money.service.WalletService;
import com.danarim.monal.money.web.dto.CreateWalletDto;
import com.danarim.monal.money.web.dto.ViewWalletDto;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Responsible for operations on {@link Wallet} entities.
 */
@RestController
@RequestMapping(WebConfig.API_V1_PREFIX + "/wallet")
@Validated
public class WalletController {

    private static final ModelMapper modelMapper = new ModelMapper();
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * Creates a new wallet for the current user.
     *
     * @param walletDto wallet data
     *
     * @return view of the created wallet
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ViewWalletDto createWallet(@RequestBody @Valid CreateWalletDto walletDto) {
        Wallet result = walletService.createWallet(walletDto, AuthUtil.getLoggedUserId());

        return modelMapper.map(result, ViewWalletDto.class);
    }

    /**
     * Returns all wallets of the current user.
     *
     * @return list of all wallets of the current user
     */
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<ViewWalletDto> getUserWallets() {
        List<Wallet> wallets = walletService.getUserWallets(AuthUtil.getLoggedUserId());

        return wallets.stream()
                .map(wallet -> modelMapper.map(wallet, ViewWalletDto.class))
                .toList();
    }

    @GetMapping("/countTransactions")
    public long countWalletTransactions(@RequestParam long walletId) {
        return walletService.countWalletTransactions(walletId, AuthUtil.getLoggedUserId());
    }

    @PutMapping("/name")
    public ViewWalletDto updateWalletName(
            @RequestParam
            Long walletId,

            @RequestParam
            @NotBlank(message = "{validation.wallet.required.name}")
            @Size(min = 2, max = 32, message = "{validation.wallet.size.name}")
            String name
    ) {
        Wallet wallet = walletService.updateWalletName(walletId, name, AuthUtil.getLoggedUserId());
        return modelMapper.map(wallet, ViewWalletDto.class);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWallet(@RequestParam long walletId) {
        walletService.deleteWallet(walletId, AuthUtil.getLoggedUserId());
    }

}
