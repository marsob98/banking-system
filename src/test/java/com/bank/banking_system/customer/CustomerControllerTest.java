package com.bank.banking_system.customer;

import com.bank.banking_system.account.AccountType;
import com.bank.banking_system.account.dto.AccountResponse;
import com.bank.banking_system.customer.dto.CustomerRequest;
import com.bank.banking_system.customer.dto.CustomerResponse;
import com.bank.banking_system.exception.ResourceNotFoundException;
import com.bank.banking_system.security.JwtService;
import com.bank.banking_system.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createCustomer_shouldReturnCreatedCustomer() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest("Jan", "Kowalski", "12345678901");
        CustomerResponse customerResponse = new CustomerResponse(1L, "Jan", "Kowalski");
        String json = objectMapper.writeValueAsString(customerRequest);
        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(customerResponse);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.pesel").doesNotExist());
    }

    @Test
    void createCustomer_shouldReturn400_whenPeselInvalid() throws Exception {
        Customer customer = new Customer(null, "Jan", "Kowalski", "1234567890");
        String json = objectMapper.writeValueAsString(customer);

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_shouldReturn400_whenPeselIsNull() throws Exception {
        CustomerRequest customerRequest = new CustomerRequest("Jan", "Kowalski", null);
        String json = objectMapper.writeValueAsString(customerRequest);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void getCustomerById_shouldReturnCustomer_whenExists() throws Exception {
        CustomerResponse customerResponse = new CustomerResponse(1L, "Jan", "Kowalski");
        when(customerService.getCustomerById(1L)).thenReturn(customerResponse);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"));

    }

    @Test
    void getCustomerById_shouldReturn404_whenNotFound() throws Exception {
        when(customerService.getCustomerById(1L)).thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/1")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Customer not found"));

    }

    @Test
    void getCustomerAccounts_shouldReturnAccounts() throws Exception {
        AccountResponse accountResponse = new AccountResponse(1L, "PL123", AccountType.SAVINGS,
                new BigDecimal("150.00"), false, "Jan");
        when(customerService.getCustomerAccounts(2L)).thenReturn(List.of(accountResponse));

        mockMvc.perform(get("/api/customers/2/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].accountNumber").value("PL123"))
                .andExpect(jsonPath("$[0].accountType").value("SAVINGS"))
                .andExpect(jsonPath("$[0].balance").value(150.00));

    }
}
