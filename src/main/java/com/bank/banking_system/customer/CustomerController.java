package com.bank.banking_system.customer;

import com.bank.banking_system.account.dto.AccountResponse;
import com.bank.banking_system.customer.dto.CustomerRequest;
import com.bank.banking_system.customer.dto.CustomerResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @GetMapping
    public List<CustomerResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @PostMapping
    public CustomerResponse createCustomer(@Valid @RequestBody CustomerRequest customerRequest) {
        return customerService.createCustomer(customerRequest);
    }

    @DeleteMapping("/{id}")
    public void deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
    }

    @GetMapping("/{id}")
    public List<AccountResponse> getCustomerAccounts(@PathVariable Long id) {
        return customerService.getCustomerAccounts(id);
    }



}
