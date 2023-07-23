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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Responsible for operations on {@link Wallet} entities.
 */
@RestController
@RequestMapping(WebConfig.API_V1_PREFIX + "/wallet")
public class WalletController {

    private final WalletService walletService;
    private static final ModelMapper modelMapper = new ModelMapper();

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

}
