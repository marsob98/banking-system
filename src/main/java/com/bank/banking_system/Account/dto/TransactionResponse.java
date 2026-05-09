package com.bank.banking_system.Account.dto;

import com.bank.banking_system.Transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private Double amount;
    private String sourceAccount;
    private String targetAccount;
    private String timestamp;
}
