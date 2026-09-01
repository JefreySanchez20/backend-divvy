package com.divvy.autenticacion.domain;

import com.divvy.shared.domain.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {

    @Test
    void registrar_conDatosValidos_creaUsuario() {
        UUID id = UUID.randomUUID();

        Usuario usuario = Usuario.registrar(id, "ana@demo.com", "hash-seguro", "Ana");

        assertThat(usuario.id()).isEqualTo(id);
        assertThat(usuario.email()).isEqualTo("ana@demo.com");
        assertThat(usuario.passwordHash()).isEqualTo("hash-seguro");
        assertThat(usuario.nombre()).isEqualTo("Ana");
        assertThat(usuario.fechaCreacion()).isNotNull();
    }

    @Test
    void registrar_conEmailInvalido_lanzaInvariantViolation() {
        assertThatThrownBy(() -> Usuario.registrar(UUID.randomUUID(), "no-es-un-email", "hash", "Ana"))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void registrar_conEmailVacio_lanzaInvariantViolation() {
        assertThatThrownBy(() -> Usuario.registrar(UUID.randomUUID(), " ", "hash", "Ana"))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void registrar_conPasswordHashVacio_lanzaInvariantViolation() {
        assertThatThrownBy(() -> Usuario.registrar(UUID.randomUUID(), "ana@demo.com", " ", "Ana"))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void registrar_conNombreVacio_lanzaInvariantViolation() {
        assertThatThrownBy(() -> Usuario.registrar(UUID.randomUUID(), "ana@demo.com", "hash", " "))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void cambiarPassword_conHashValido_actualizaElHash() {
        Usuario usuario = Usuario.registrar(UUID.randomUUID(), "ana@demo.com", "hash-viejo", "Ana");

        usuario.cambiarPassword("hash-nuevo");

        assertThat(usuario.passwordHash()).isEqualTo("hash-nuevo");
    }

    @Test
    void cambiarPassword_conHashVacio_lanzaInvariantViolation() {
        Usuario usuario = Usuario.registrar(UUID.randomUUID(), "ana@demo.com", "hash-viejo", "Ana");

        assertThatThrownBy(() -> usuario.cambiarPassword(" "))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void reconstruir_reconstruyeSinReaplicarReglasDeCreacion() {
        UUID id = UUID.randomUUID();
        Instant fecha = Instant.parse("2026-01-01T00:00:00Z");

        Usuario usuario = Usuario.reconstruir(id, "ana@demo.com", "hash", "Ana", fecha);

        assertThat(usuario.id()).isEqualTo(id);
        assertThat(usuario.fechaCreacion()).isEqualTo(fecha);
    }
}
