package com.bank.banking_system.security;

import com.bank.banking_system.SecurityConfig;
import com.bank.banking_system.account.AccountController;
import com.bank.banking_system.account.AccountService;
import com.bank.banking_system.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, JwtAccessDeniedHandler.class, JwtAuthEntryPoint.class})
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;


    @Test
    void protectedEndpoint_shouldReturn401_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/accounts/my").with(user("user").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEndpoint_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(delete("/api/accounts/1").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEndpoint_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(delete("/api/accounts/1").with(user("user").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void blockAccount_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(put("/api/accounts/1/block").with(user("Nowak").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllAccounts_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(get("/api/accounts").with(user("Nowak").roles("USER")))
                .andExpect(status().isForbidden());

        verify(accountService, never()).getAllAccounts();
    }

    @Test
    void withdraw_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(put("/api/accounts/1/withdraw")
                        .param("amount", "50.00")
                        .with(user("Nowak").roles("USER")))
                .andExpect(status().isForbidden());

        verify(accountService, never()).withdraw(any(),any());

    }

    @Test
    void deposit_shouldReturn403_whenUser() throws Exception {
        mockMvc.perform(put("/api/accounts/1/deposit")
                        .param("amount", "50.00")
                        .with(user("Nowak").roles("USER")))
                .andExpect(status().isForbidden());

        verify(accountService, never()).deposit(any(),any());
    }


}