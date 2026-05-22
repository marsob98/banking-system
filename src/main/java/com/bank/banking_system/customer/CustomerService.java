package com.bank.banking_system.customer;

import com.bank.banking_system.account.Account;
import com.bank.banking_system.account.AccountRepository;
import com.bank.banking_system.account.AccountService;
import com.bank.banking_system.account.dto.AccountResponse;
import com.bank.banking_system.exception.AccountHasActiveAccountsException;
import com.bank.banking_system.exception.DuplicatePeselException;
import com.bank.banking_system.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public CustomerService(CustomerRepository customerRepository,
                           AccountRepository accountRepository,
                           AccountService accountService) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        customerRepository.findByPesel(customer.getPesel())
                .ifPresent(existing -> {throw new DuplicatePeselException("Pesel already exists");
                });

        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Account> accounts = accountRepository.findByCustomerId(id);

        if (!accounts.isEmpty()) {
            throw new AccountHasActiveAccountsException(
                    "Customers has " + accounts.size() + " account(s). Delete first."
            );
        }
        customerRepository.delete(customer);
    }

    @Transactional
    public List<AccountResponse> getCustomerAccounts(Long id) {
        return accountRepository.findByCustomerId(id).stream()
                .map(accountService::toResponse)
                .toList();
    }

}
