package com.bank.banking_system.Account.dto;

public class TransactionResponse {
    private Long id;
    private String type;
    private Double amount;
    private String sourceAccount;
    private String targetAccount;
    private String timestamp;

    public TransactionResponse(Long id, String type, Double amount, String sourceAccount, String targetAccount, String timestamp) {
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

    public String getType() {
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
