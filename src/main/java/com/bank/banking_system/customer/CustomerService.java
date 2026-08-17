package com.bank.banking_system.customer;

import com.bank.banking_system.account.Account;
import com.bank.banking_system.account.AccountRepository;
import com.bank.banking_system.account.AccountService;
import com.bank.banking_system.account.dto.AccountResponse;
import com.bank.banking_system.customer.dto.CustomerRequest;
import com.bank.banking_system.customer.dto.CustomerResponse;
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
    public CustomerResponse createCustomer(CustomerRequest customerRequest) {
        customerRepository.findByPesel(customerRequest.pesel())
                .ifPresent(existing -> {throw new DuplicatePeselException("Pesel already exists");
                });
        Customer customer = new Customer(null, customerRequest.firstName(), customerRequest.lastName(), customerRequest.pesel());
        Customer saved = customerRepository.save(customer);
        return toCustomerResponse(saved);
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

    @Transactional
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::toCustomerResponse)
                .toList();
    }


    public CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getFirstName(), customer.getLastName());
    }

}
