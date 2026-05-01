package com.bank.banking_system.Account.dto;

public class BankStatsResponse {
    private Long totalCustomers;
    private Long totalAccount;
    private Long totalTransactions;
    private Double totalMoney;

    public BankStatsResponse(Long totalCustomers, Long totalAccount, Long totalTransactions, Double totalMoney) {
        this.totalCustomers = totalCustomers;
        this.totalAccount = totalAccount;
        this.totalTransactions = totalTransactions;
        this.totalMoney = totalMoney;
    }

    public Long getTotalCustomers() {
        return totalCustomers;
    }

    public Long getTotalAccount() {
        return totalAccount;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public Double getTotalMoney() {
        return totalMoney;
    }
}
