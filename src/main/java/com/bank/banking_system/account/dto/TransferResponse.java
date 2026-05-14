package com.bank.banking_system.account.dto;


import java.math.BigDecimal;

public record TransferResponse(
        String message,
        BigDecimal amount,
        String fromAccount,
        String toAccount1
) {

}



