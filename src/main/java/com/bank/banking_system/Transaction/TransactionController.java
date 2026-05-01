package com.bank.banking_system.Transaction;

import com.bank.banking_system.Account.AccountService;
import com.bank.banking_system.Account.dto.TransactionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionController(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(accountService::toTransactionResponse)
                .toList();
    }

}

