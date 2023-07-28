package com.danarim.monal.money.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * DTO for creating a {@link com.danarim.monal.money.persistence.model.Transaction Transaction}.
 *
 * @param description A description of the transaction.
 * @param date        The date of the transaction.
 * @param amount      The amount of the transaction. Can't be negative or zero.
 * @param categoryId  The ID of the category of the transaction.
 * @param walletId    The ID of the wallet the transaction belongs to.
 */
public record CreateTransactionDto(

        @Size(max = 255, message = "{validation.transaction.description.size}")
        String description,

        @NotNull(message = "{validation.transaction.date.notnull}")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        Date date,

        @Positive(message = "{validation.transaction.amount.positive}")
        double amount,

        @NotNull(message = "{validation.transaction.categoryId.notnull}")
        long categoryId,

        @NotNull(message = "{validation.transaction.walletId.notnull}")
        long walletId
) {

}
