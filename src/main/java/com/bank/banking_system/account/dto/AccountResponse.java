package com.bank.banking_system.account.dto;

import com.bank.banking_system.account.AccountType;
import java.math.BigDecimal;


public record AccountResponse(
        Long id,
        String accountNumber,
        AccountType accountType,
        BigDecimal balance,
        Boolean blocked,
        String ownerName
) {

}
