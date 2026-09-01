package com.divvy.shared.infrastructure.events;

import com.divvy.shared.domain.DomainEvent;
import com.divvy.shared.domain.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher springPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publicar(DomainEvent event) {
        springPublisher.publishEvent(event);
    }
}
