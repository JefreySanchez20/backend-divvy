package com.divvy.shared.domain.exception;

public class InvariantViolationException extends DomainException {

    public InvariantViolationException(String message) {
        super("INVARIANT_VIOLATED", message);
    }
}
