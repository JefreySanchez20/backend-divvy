package com.divvy.shared.domain.exception;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message);
    }
}
