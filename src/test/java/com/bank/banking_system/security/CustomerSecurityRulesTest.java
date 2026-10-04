package com.bank.banking_system.security;

import com.bank.banking_system.SecurityConfig;
import com.bank.banking_system.customer.CustomerController;
import com.bank.banking_system.customer.CustomerService;
import com.bank.banking_system.customer.dto.CustomerResponse;
import com.bank.banking_system.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import({SecurityConfig.class, JwtAuthEntryPoint.class, JwtAccessDeniedHandler.class})
public class CustomerSecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    private static final long ANY_CUSTOMER_ID = 1L;

    @Test
    void customerList_shouldReturn403_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/api/customers").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());

    }

    @Test
    void customerById_shouldReturn403_whenRoleIsUser() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", ANY_CUSTOMER_ID).with(user("user").roles("USER")))
                .andExpect(status().isForbidden());

    }

    @Test
    void customerAccounts_shouldReturn403_whenRoleIsUser() throws Exception {

        mockMvc.perform(get("/api/customers/{id}/accounts", ANY_CUSTOMER_ID).with(user("user").roles("USER")))
                .andExpect(status().isForbidden());

    }

    @Test
    void customerMe_shouldReturn200_whenRoleIsUser() throws Exception {
        CustomerResponse customerResponse = new CustomerResponse(1L, "Jan", "Kowalski");
        when(customerService.getCustomerByUsername("Janusz")).thenReturn(customerResponse);

        mockMvc.perform(get("/api/customers/me").with(user("Janusz").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jan"))
                .andExpect(jsonPath("$.lastName").value("Kowalski"));
    }





}
