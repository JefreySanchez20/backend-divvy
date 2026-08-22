package com.divvy.shared.domain.exception;

public class EntityNotFoundException extends DomainException {

    public EntityNotFoundException(String message) {
        super("ENTITY_NOT_FOUND", message);
    }
}
