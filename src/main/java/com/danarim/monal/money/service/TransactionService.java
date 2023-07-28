package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.web.dto.CreateTransactionDto;

import java.util.Date;
import java.util.List;

/**
 * Service for {@link Transaction Transaction}.
 */
public interface TransactionService {

    Transaction createTransaction(CreateTransactionDto createTransactionDto, long userId);

    List<Transaction> getTransactionsBetweenDates(Date from,
                                                  Date to,
                                                  long walletId,
                                                  long loggedUserId
    );

}
