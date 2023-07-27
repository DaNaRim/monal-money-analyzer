package com.danarim.monal.money.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.auth.AuthUtil;
import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.service.TransactionService;
import com.danarim.monal.money.web.dto.CreateTransactionDto;
import com.danarim.monal.money.web.dto.ViewTransactionDto;
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
 * Controller for {@link Transaction Transaction}.
 */
@RestController
@RequestMapping(WebConfig.API_V1_PREFIX + "/transaction")
public class TransactionController {

    private static final ModelMapper modelMapper = new ModelMapper();
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Creates a new transaction.
     *
     * @param transDto DTO with transaction data
     *
     * @return DTO with created transaction data
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ViewTransactionDto createTransaction(@RequestBody @Valid CreateTransactionDto transDto) {
        Transaction transaction = transactionService.createTransaction(
                transDto,
                AuthUtil.getLoggedUserId()
        );
        return modelMapper.map(transaction, ViewTransactionDto.class);
    }

}
