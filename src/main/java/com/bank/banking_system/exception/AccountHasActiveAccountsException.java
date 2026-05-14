package com.bank.banking_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class AccountHasActiveAccountsException extends RuntimeException {
    public AccountHasActiveAccountsException(String message) {
        super(message);
    }
}
