package com.bank.banking_system.Account;

import com.bank.banking_system.Customer.Customer;
import com.bank.banking_system.Customer.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Account createAccount(String accountType, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        String accountNumber = "PL" + System. currentTimeMillis();

        Account account = new Account(accountNumber, accountType, customer);
        return accountRepository.save(account);
    }

    public Account deposit(Long accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance() + amount);
        return accountRepository.save(account);
    }

    public Account withdraw(Long accountId, Double amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance() - amount < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(account.getBalance() - amount);
        return accountRepository.save(account);
    }

    public void transfer(Long fromAccId, Long toAccId, Double amount) {
        Account source = accountRepository.findById(fromAccId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Account target = accountRepository.findById(toAccId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (source.getBalance() - amount < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        source.setBalance(source.getBalance() - amount);
        target.setBalance(target.getBalance() + amount);

        accountRepository.save(source);
        accountRepository.save(target);
    }

}
