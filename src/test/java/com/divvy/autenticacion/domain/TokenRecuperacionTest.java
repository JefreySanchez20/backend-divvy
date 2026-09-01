package com.divvy.autenticacion.domain;

import com.divvy.shared.domain.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenRecuperacionTest {

    @Test
    void generar_creaTokenVigenteYNoUsado() {
        TokenRecuperacion token = TokenRecuperacion.generar(UUID.randomUUID(), UUID.randomUUID(), Duration.ofMinutes(30));

        assertThat(token.usado()).isFalse();
        assertThat(token.estaVigente()).isTrue();
        assertThat(token.token()).matches("[23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6}");
    }

    @Test
    void estaVigente_conFechaExpiracionPasada_devuelveFalse() {
        TokenRecuperacion token = TokenRecuperacion.reconstruir(
                UUID.randomUUID(), UUID.randomUUID(), "abc123", Instant.now().minusSeconds(60), false);

        assertThat(token.estaVigente()).isFalse();
    }

    @Test
    void estaVigente_yaUsado_devuelveFalse() {
        TokenRecuperacion token = TokenRecuperacion.reconstruir(
                UUID.randomUUID(), UUID.randomUUID(), "abc123", Instant.now().plusSeconds(60), true);

        assertThat(token.estaVigente()).isFalse();
    }

    @Test
    void marcarComoUsado_tokenVigente_loMarcaComoUsado() {
        TokenRecuperacion token = TokenRecuperacion.generar(UUID.randomUUID(), UUID.randomUUID(), Duration.ofMinutes(30));

        token.marcarComoUsado();

        assertThat(token.usado()).isTrue();
        assertThat(token.estaVigente()).isFalse();
    }

    @Test
    void marcarComoUsado_tokenExpirado_lanzaInvariantViolation() {
        TokenRecuperacion token = TokenRecuperacion.reconstruir(
                UUID.randomUUID(), UUID.randomUUID(), "abc123", Instant.now().minusSeconds(60), false);

        assertThatThrownBy(token::marcarComoUsado).isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void marcarComoUsado_tokenYaUsado_lanzaInvariantViolation() {
        TokenRecuperacion token = TokenRecuperacion.generar(UUID.randomUUID(), UUID.randomUUID(), Duration.ofMinutes(30));
        token.marcarComoUsado();

        assertThatThrownBy(token::marcarComoUsado).isInstanceOf(InvariantViolationException.class);
    }
}
