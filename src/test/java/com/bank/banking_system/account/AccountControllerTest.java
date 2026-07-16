package com.bank.banking_system.account;

import com.bank.banking_system.SecurityConfig;
import com.bank.banking_system.account.dto.AccountResponse;
import com.bank.banking_system.account.dto.TransactionResponse;
import com.bank.banking_system.customer.Customer;
import com.bank.banking_system.exception.ResourceNotFoundException;
import com.bank.banking_system.security.JwtAccessDeniedHandler;
import com.bank.banking_system.security.JwtAuthEntryPoint;
import com.bank.banking_system.security.JwtService;
import com.bank.banking_system.transaction.TransactionType;
import com.bank.banking_system.user.CustomUserDetailsService;
import com.bank.banking_system.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AccountRepository accountRepository;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockitoBean
    private JwtAuthEntryPoint jwtAuthEntryPoint;


    @Test
    void createAccount_shouldReturnCreatedAccount() throws Exception {
        Customer customer = new Customer(1L, "Jan", "Kowalski", "12345678901");
        Account account = new Account("PL123", AccountType.CHECKING, customer);
        AccountResponse response = new AccountResponse(
                1L, "PL123", AccountType.CHECKING,
                new BigDecimal("100.00"), false, "Jan Kowalski");

        when(accountService.createAccount(AccountType.SAVINGS, 1L)).thenReturn(account);
        when(accountService.toResponse(any(Account.class))).thenReturn(response);

        mockMvc.perform(post("/api/accounts").with(user("Nowak").roles("USER"))
                        .param("accountType", "SAVINGS")
                        .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("PL123"));

    }

    @Test
    void getAllAccounts_shouldReturnList() throws Exception {
        AccountResponse response = new AccountResponse(
                1L, "PL123", AccountType.CHECKING,
                new BigDecimal("100.00"), false, "Jan Kowalski");
        when(accountService.getAllAccounts()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accounts").with(user("Nowak").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("PL123"))
                .andExpect(jsonPath("$[0].balance").value(100.00));
    }

    @Test
    void getAllTransactionsForAcc_shouldReturnTransactionList() throws Exception {
        TransactionResponse response = new TransactionResponse(
                1L, TransactionType.DEPOSIT, new BigDecimal("100.00"),
                null, "PL123", LocalDateTime.now());

        when(accountService.getAllTransactionsForAcc(1L, "Nowak")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/accounts/1/transactions").with(user("Nowak").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].targetAccount").value("PL123"));
    }

    @Test
    void findAccountByNumber_shouldReturn404_whenNotFound() throws Exception {
        when(accountService.findAccountByNumber("PL123"))
                .thenThrow(new ResourceNotFoundException("Account not found"));

        mockMvc.perform(get("/api/accounts/number/PL123").with(user("Nowak").roles("USER")))
                .andExpect(status().isNotFound());
    }
}