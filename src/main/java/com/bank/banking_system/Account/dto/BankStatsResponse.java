package com.bank.banking_system.Account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BankStatsResponse {
    private Long totalCustomers;
    private Long totalAccount;
    private Long totalTransactions;
    private Double totalMoney;
}
