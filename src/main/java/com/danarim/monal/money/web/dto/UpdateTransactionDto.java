package com.danarim.monal.money.web.dto;

import com.danarim.monal.exceptions.ValidationCodes;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import static com.danarim.monal.exceptions.ValidationCodes.TRANSACTION_WALLET_ID_REQUIRED;

/**
 * DTO for updating a {@link com.danarim.monal.money.persistence.model.Transaction Transaction}.
 *
 * @param id          The ID of the transaction.
 * @param description A description of the transaction.
 * @param date        The date of the transaction.
 * @param amount      The amount of the transaction. Can't be negative or zero.
 * @param categoryId  The ID of the category of the transaction.
 * @param walletId    The ID of the wallet the transaction belongs to.
 */
public record UpdateTransactionDto(

        long id,

        @Size(max = 255, message = ValidationCodes.TRANSACTION_DESCRIPTION_SIZE)
        String description,

        @NotNull(message = ValidationCodes.TRANSACTION_DATE_REQUIRED)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        Date date,

        @Positive(message = ValidationCodes.TRANSACTION_AMOUNT_POSITIVE)
        double amount,

        @NotNull(message = ValidationCodes.TRANSACTION_CATEGORY_ID_REQUIRED)
        long categoryId,

        @NotNull(message = TRANSACTION_WALLET_ID_REQUIRED)
        long walletId
) {

}
