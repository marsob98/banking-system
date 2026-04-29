package com.bank.banking_system.Account.dto;

public class AccountResponse {
    private Long id;
    private String accountNumber;
    private String accountType;
    private Double balance;
    private Boolean blocked;
    private String ownerName;

    public AccountResponse(Long id, String accountNumber, String accountType,
                           Double balance, Boolean blocked, String ownerName) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.blocked = blocked;
        this.ownerName = ownerName;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public Double getBalance() {
        return balance;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
