package com.bank.banking_system.customer;

import com.bank.banking_system.customer.dto.CustomerRequest;
import com.bank.banking_system.customer.dto.CustomerResponse;
import com.bank.banking_system.security.JwtService;
import com.bank.banking_system.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
}
