package com.danarim.monal.money.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.config.security.auth.AuthUtil;
import com.danarim.monal.money.persistence.model.Transaction;
import com.danarim.monal.money.service.TransactionService;
import com.danarim.monal.money.web.dto.CreateTransactionDto;
import com.danarim.monal.money.web.dto.UpdateTransactionDto;
import com.danarim.monal.money.web.dto.ViewTransactionDto;
import org.modelmapper.ModelMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
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

    /**
     * Returns all transactions for the specified day and wallet.
     * <p> We use two dates because server handles dates in UTC time zone and the client in local
     * time zone. </p>
     *
     * @param from     Date in format 'yyyy-MM-dd hh' in UTC time zone.
     * @param to       Date in format 'yyyy-MM-dd hh' in UTC time zone.
     * @param walletId Wallet ID.
     *
     * @return List of transactions for the specified day.
     */
    @GetMapping(
            path = "/date",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<ViewTransactionDto> getTransactionsBetweenDates(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH") Date from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH") Date to,
            @RequestParam long walletId
    ) {
        List<Transaction> transactions = transactionService.getTransactionsBetweenDates(
                from,
                to,
                walletId,
                AuthUtil.getLoggedUserId()
        );
        return transactions.stream()
                .map(transaction -> modelMapper.map(transaction, ViewTransactionDto.class))
                .sorted()
                .toList();
    }

    /**
     * Updates a transaction.
     *
     * @param updateTransactionDto The DTO with the updated transaction data.
     *
     * @return The DTO with the updated transaction data.
     */
    @PutMapping
    public ViewTransactionDto updateTransaction(
            @RequestBody @Valid UpdateTransactionDto updateTransactionDto
    ) {
        Transaction transaction = transactionService.updateTransaction(
                updateTransactionDto,
                AuthUtil.getLoggedUserId());

        return modelMapper.map(transaction, ViewTransactionDto.class);
    }

    @DeleteMapping
    public void deleteTransaction(@RequestParam long transactionId) {
        transactionService.deleteTransaction(transactionId, AuthUtil.getLoggedUserId());
    }

}
