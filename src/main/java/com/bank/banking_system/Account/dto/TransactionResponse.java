package com.bank.banking_system.Account.dto;

import com.bank.banking_system.Transaction.TransactionType;

public class TransactionResponse {
    private Long id;
    private TransactionType type;
    private Double amount;
    private String sourceAccount;
    private String targetAccount;
    private String timestamp;

    public TransactionResponse(Long id, TransactionType type, Double amount, String sourceAccount, String targetAccount, String timestamp) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public Double getAmount() {
        return amount;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
