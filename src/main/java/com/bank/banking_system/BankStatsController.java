package com.bank.banking_system;

import com.bank.banking_system.account.AccountService;
import com.bank.banking_system.account.dto.BankStatsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/stats")
public class BankStatsController {
    private final AccountService accountService;

    public BankStatsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public BankStatsResponse bankStatsResponse() {
         return accountService.bankStatsResponse();
    }

    @GetMapping("/sum")
    public BigDecimal getAllBalances() {
        return accountService.sumAllBalances();
    }


}
