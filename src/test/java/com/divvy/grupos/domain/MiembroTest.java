package com.divvy.grupos.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MiembroTest {

    @Test
    void constructor_conUsuarioIdNulo_lanzaNullPointerException() {
        assertThatThrownBy(() -> new Miembro(null, Rol.MIEMBRO, Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_conRolNulo_lanzaNullPointerException() {
        assertThatThrownBy(() -> new Miembro(UUID.randomUUID(), null, Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equals_conMismosValores_sonIguales() {
        UUID usuarioId = UUID.randomUUID();
        Instant fecha = Instant.now();

        Miembro m1 = new Miembro(usuarioId, Rol.ADMIN, fecha);
        Miembro m2 = new Miembro(usuarioId, Rol.ADMIN, fecha);

        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }

    @Test
    void equals_conRolDistinto_noSonIguales() {
        UUID usuarioId = UUID.randomUUID();
        Instant fecha = Instant.now();

        Miembro admin = new Miembro(usuarioId, Rol.ADMIN, fecha);
        Miembro miembro = new Miembro(usuarioId, Rol.MIEMBRO, fecha);

        assertThat(admin).isNotEqualTo(miembro);
    }
}
