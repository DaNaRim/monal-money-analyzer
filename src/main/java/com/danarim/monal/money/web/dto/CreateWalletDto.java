package com.danarim.monal.money.web.dto;

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

        @NotBlank(message = "{validation.wallet.required.name}")
        @Size(min = 2, max = 32, message = "{validation.wallet.size.name}")
        String name,

        @Max(value = 1_000_000_000L, message = "{validation.wallet.max.balance}")
        @Min(value = -1_000_000_000L, message = "{validation.wallet.min.balance}")
        double balance,

        @NotBlank(message = "{validation.wallet.required.currency}")
        String currency
) {

}
