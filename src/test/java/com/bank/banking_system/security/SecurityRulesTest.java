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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        mockMvc.perform(get("/api/accounts").with(user("user").roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void deleteEndpoint_shouldReturn403_whenUserNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/accounts/1").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteEndpoint_shouldReturn200_whenAdmin() throws Exception {
        mockMvc.perform(delete("/api/accounts/1").with(user("user").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}