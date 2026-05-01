package com.bank.banking_system;

import com.bank.banking_system.Account.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class BankStatsController {
    private final AccountService accountService;

    public BankStatsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public void bankStatsResponse() {
        accountService.bankStatsResponse();
    }
}
