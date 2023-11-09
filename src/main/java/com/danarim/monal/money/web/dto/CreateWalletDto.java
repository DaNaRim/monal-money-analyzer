package com.danarim.monal.money.web.dto;

import com.danarim.monal.exceptions.ValidationCodes;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data for creating a wallet.
 *
 * @param name    The name of the wallet.
 * @param balance Initial balance of the wallet. Can be negative.
 */
public record CreateWalletDto(

        @NotBlank(message = ValidationCodes.WALLET_NAME_REQUIRED)
        @Size(min = 2, max = 32, message = ValidationCodes.WALLET_NAME_SIZE)
        String name,

        @Max(value = 1_000_000_000L, message = ValidationCodes.WALLET_BALANCE_MAX)
        @Min(value = -1_000_000_000L, message = ValidationCodes.WALLET_BALANCE_MIN)
        double balance,

        @NotBlank(message = ValidationCodes.WALLET_CURRENCY_REQUIRED)
        String currency
) {

}
