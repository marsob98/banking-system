package com.bank.banking_system.account.dto;

import com.bank.banking_system.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        String sourceAccount,
        String targetAccount,
        LocalDateTime timestamp
) {

}
