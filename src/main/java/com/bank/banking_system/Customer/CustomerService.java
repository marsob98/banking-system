package com.bank.banking_system.Customer;

import com.bank.banking_system.Account.Account;
import com.bank.banking_system.Account.AccountRepository;
import com.bank.banking_system.Account.AccountService;
import com.bank.banking_system.Account.dto.AccountResponse;
import com.bank.banking_system.Exception.DuplicatePeselException;
import com.bank.banking_system.Exception.ResourceNotFoundException;
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
        customerRepository.delete(customer);
    }

    @Transactional
    public List<AccountResponse> CustomerDetailsResponse(Long id) {
        return accountRepository.findByCustomerId(id).stream()
                .map(accountService::toResponse).toList();
    }

}
