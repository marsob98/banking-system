package com.bank.banking_system.account.dto;

import java.math.BigDecimal;


public record BankStatsResponse(
        Long totalCustomers,
        Long totalAccount,
        Long totalTransactions,
        BigDecimal totalMoney
) {

}
