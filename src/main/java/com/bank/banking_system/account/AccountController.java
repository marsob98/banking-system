package com.bank.banking_system.account;

import com.bank.banking_system.account.dto.AccountResponse;
import com.bank.banking_system.account.dto.TransactionResponse;
import com.bank.banking_system.account.dto.TransferResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public List<TransactionResponse> getAllTransactionsForAcc(@PathVariable Long id,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        return accountService.getAllTransactionsForAcc(id, userDetails.getUsername());
    }

    @GetMapping("/number/{accountNumber}")
    public AccountResponse findAccountByNumber(@PathVariable String accountNumber) {
        Account accountByNumber = accountService.findAccountByNumber(accountNumber);
        return accountService.toResponse(accountByNumber);
    }

    @GetMapping("/my")
    public List<AccountResponse> getMyAccounts(@AuthenticationPrincipal UserDetails userDetails) {
        return accountService.getAccountsForUser(userDetails.getUsername());
    }

    @PostMapping
    public AccountResponse createAccount(@RequestParam AccountType accountType, @RequestParam Long customerId) {
        Account account = accountService.createAccount(accountType, customerId);
        return accountService.toResponse(account);
    }

    @PutMapping("/{id}/deposit")
    public AccountResponse deposit(@PathVariable Long id, @RequestParam BigDecimal amount) {
        Account account = accountService.deposit(id, amount);
        return accountService.toResponse(account);
    }

    @PutMapping("/{id}/withdraw")
    public AccountResponse withdraw(@PathVariable Long id,
                                    @RequestParam BigDecimal amount,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Account account = accountService.withdraw(id, amount, userDetails.getUsername());
        return accountService.toResponse(account);
    }

    @PutMapping("/transfer")
    public TransferResponse transfer(@RequestParam Long fromId,
                                     @RequestParam Long toId,
                                     @RequestParam BigDecimal amount,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        return accountService.transfer(fromId, toId, amount, userDetails.getUsername());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/block")
    public AccountResponse blockAccount(@PathVariable Long id) {
        Account account = accountService.blockAccount(id);
        return accountService.toResponse(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/unblock")
    public AccountResponse unblockAccount(@PathVariable Long id) {
        Account account = accountService.unBlockAccount(id);
        return accountService.toResponse(account);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping({"/{id}"})
    public void deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
    }





}
