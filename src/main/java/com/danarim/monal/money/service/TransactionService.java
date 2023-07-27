package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.web.dto.CreateTransactionDto;

/**
 * Service for {@link Transaction Transaction}.
 */
public interface TransactionService {

    Transaction createTransaction(CreateTransactionDto createTransactionDto, long userId);

}
