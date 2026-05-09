package com.bank.banking_system.Account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransferResponse {
    private String message;
    private Double amount;
    private String fromAccount;
    private String toAccount;


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
