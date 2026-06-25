package com.bank.banking_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SomeoneElseAccountException extends RuntimeException {
    public SomeoneElseAccountException(String message) {
        super(message);
    }
}
