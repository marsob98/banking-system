package com.bank.banking_system.Account.dto;

public class TransferResponse {
    private String message;
    private Double amount;
    private String fromAccount;
    private String toAccount;

    public TransferResponse(String message, Double amount, String fromAccount, String toAccount) {
        this.message = message;
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public String getMessage() {
        return message;
    }

    public Double getAmount() {
        return amount;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }
}
