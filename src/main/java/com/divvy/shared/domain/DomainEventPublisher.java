package com.divvy.shared.domain;

public interface DomainEventPublisher {

    void publicar(DomainEvent event);
}
