package com.bank.banking_system.security;

import com.bank.banking_system.SecurityConfig;
import com.bank.banking_system.account.AccountController;
import com.bank.banking_system.account.AccountService;
import com.bank.banking_system.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockitoBean
    private JwtAuthEntryPoint jwtAuthEntryPoint;

    @Test
    void protectedEndpoint_shouldReturn401_whenNoAuth() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void protectedEndpoint_shouldReturn200_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteEndpoint_shouldReturn403_whenUserNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteEndpoint_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(delete("/api/accounts/1"))
                .andExpect(status().isOk());
    }
}