package com.divvy.shared.infrastructure.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class LimpiadorTokensRevocados {

    private static final Logger log = LoggerFactory.getLogger(LimpiadorTokensRevocados.class);

    private final RevokedTokenJpaRepository jpaRepository;

    public LimpiadorTokensRevocados(RevokedTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void limpiarExpirados() {
        jpaRepository.eliminarExpirados(Instant.now());
        log.debug("Limpieza de tokens revocados expirados ejecutada");
    }
}
