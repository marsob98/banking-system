package com.bank.banking_system.customer;

import com.bank.banking_system.account.Account;
import com.bank.banking_system.account.AccountRepository;
import com.bank.banking_system.account.AccountService;
import com.bank.banking_system.account.AccountType;
import com.bank.banking_system.account.dto.AccountResponse;
import com.bank.banking_system.exception.AccountHasActiveAccountsException;
import com.bank.banking_system.exception.DuplicatePeselException;
import com.bank.banking_system.transaction.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private CustomerService customerService;


    @Test
    void createCustomer_shouldThrow_whenPeselAlreadyInDataBase() {
        Customer customer = new Customer(1L, "Jan", "Kowalski", "12345678901");
        when(customerRepository.findByPesel(customer.getPesel())).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> customerService.createCustomer(customer))
                .isInstanceOf(DuplicatePeselException.class);
    }

    @Test
    void deleteCustomer_shouldThrow_whenCustomerHasAccounts() {
        Customer customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        Account account = new Account("PL123", AccountType.CHECKING, customer);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerId(1L)).thenReturn(List.of(account));

        assertThatThrownBy(() -> customerService.deleteCustomer(1L))
                .isInstanceOf(AccountHasActiveAccountsException.class);
    }

    @Test
    void getCustomerAccounts_shouldReturnList_whenCustomerExists() {
        AccountResponse fakeResponse = new AccountResponse(
                1L,
                "PL123",
                AccountType.CHECKING,
                new BigDecimal("100.00"),
                false,
                "Jan Kowalski");

        when(accountRepository.findByCustomerId(1L)).thenReturn(List.of(new Account(
                "PL123",
                AccountType.CHECKING,
                new Customer())));

        when(accountService.toResponse(any(Account.class)))
                .thenReturn(fakeResponse);

        List<AccountResponse> result = customerService.getCustomerAccounts(1L);

        assertThat(result.get(0).id()).isEqualTo(1L);

    }



}
