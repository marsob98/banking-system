package com.bank.banking_system.security;

import com.bank.banking_system.BankStatsController;
import com.bank.banking_system.SecurityConfig;
import com.bank.banking_system.account.AccountService;
import com.bank.banking_system.transaction.TransactionController;
import com.bank.banking_system.transaction.TransactionRepository;
import com.bank.banking_system.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@WebMvcTest({TransactionController.class, BankStatsController.class})
@Import({SecurityConfig.class, JwtAccessDeniedHandler.class, JwtAuthEntryPoint.class})
public class AdminEndpointsSecurityTest {

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllTransactions_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(get("/api/transactions").with(user("Nowak").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTransactions_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/transactions").with(user("Admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBalances_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(get("/api/stats/sum").with(user("Nowak").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void bankStatsResponse_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(get("/api/stats").with(user("Nowak").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void bankStatsResponse_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(get("/api/stats").with(user("Admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
