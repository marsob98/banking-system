package com.bank.banking_system.account.dto;

import jakarta.validation.constraints.NotBlank;

public record RecipientVerificationRequest(
        @NotBlank(message = "account number is required") String accountNumber,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName
        ) {

}
