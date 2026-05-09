package com.bank.banking_system.Exception;

public class AccountHasActiveAccountsException extends RuntimeException {
    public AccountHasActiveAccountsException(String message) {
        super(message);
    }
}
