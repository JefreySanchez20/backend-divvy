package com.divvy.shared.domain.exception;

public class UnauthorizedOperationException extends DomainException {

    public UnauthorizedOperationException(String message) {
        super("UNAUTHORIZED_OPERATION", message);
    }
}
