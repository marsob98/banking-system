package com.bank.banking_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePeselException extends RuntimeException {

    public DuplicatePeselException(String message) {
        super(message);
    }
}
