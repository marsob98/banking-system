package com.bank.banking_system.Account.dto;

import com.bank.banking_system.Account.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private Double balance;
    private Boolean blocked;
    private String ownerName;
}
