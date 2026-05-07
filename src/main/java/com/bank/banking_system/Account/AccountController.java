package com.bank.banking_system.Account;

import com.bank.banking_system.Account.dto.AccountResponse;
import com.bank.banking_system.Account.dto.TransactionResponse;
import com.bank.banking_system.Account.dto.TransferResponse;
import com.bank.banking_system.Transaction.TransactionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> getAllTransactionsForAcc(@PathVariable Long id) {
        return accountService.getAllTransactionsForAcc(id);
    }

    @GetMapping("/number/{accountNumber}")
    public AccountResponse findAccountByNumber(@PathVariable String accountNumber) {
        Account accountByNumber = accountService.findAccountByNumber(accountNumber);
        return accountService.toResponse(accountByNumber);
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
        return accountService.transfer(fromId, toId, amount);
    }

    @PutMapping("/{id}/block")
    public AccountResponse blockAccount(@PathVariable Long id) {
        Account account = accountService.blockAccount(id);
        return accountService.toResponse(account);
    }

    @PutMapping("/{id}/unblock")
    public AccountResponse unblockAccount(@PathVariable Long id) {
        Account account = accountService.unBlockAccount(id);
        return accountService.toResponse(account);
    }

    @DeleteMapping({"/{id}"})
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }




}
