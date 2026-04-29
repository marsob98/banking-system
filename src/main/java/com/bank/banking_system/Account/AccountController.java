package com.bank.banking_system.Account;

import com.bank.banking_system.Account.dto.AccountResponse;
import com.bank.banking_system.Account.dto.TransferResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountRepository accountRepository; 
    private final AccountService accountService;


    public AccountController(AccountRepository accountRepository, AccountService accountService) {
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }


    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream().map(accountService::toResponse).toList();
    }

    @PostMapping
    public AccountResponse createAccount(@RequestParam String accountType, @RequestParam Long customerId) {
        Account account = accountService.createAccount(accountType, customerId);
        return accountService.toResponse(account);
    }

    @PutMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable Long id, @RequestParam Double amount) {
        Account account = accountService.deposit(id, amount);
        return accountService.toResponse(account);
    }

    @PutMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable Long id, @RequestParam Double amount) {
        Account account = accountService.withdraw(id, amount);
        return accountService.toResponse(account);
    }

    @PutMapping("/transfer")
    public TransferResponse transfer(@RequestParam Long fromId,
                                     @RequestParam Long toId,
                                     @RequestParam Double amount) {
        accountService.transfer(fromId, toId, amount);
        return accountService.transfer(fromId, toId, amount);
    }
}
