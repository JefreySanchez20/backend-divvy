package com.divvy.grupos.domain;

import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.InvariantViolationException;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrupoTest {

    @Test
    void crear_conNombreValido_generaGrupoConCreadorComoUnicoAdmin() {
        UUID creadorId = UUID.randomUUID();

        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);

        assertThat(grupo.nombre()).isEqualTo("Roomies");
        assertThat(grupo.estado()).isEqualTo(EstadoGrupo.ACTIVO);
        assertThat(grupo.miembros()).hasSize(1);
        assertThat(grupo.esAdmin(creadorId)).isTrue();
        assertThat(grupo.tieneMiembro(creadorId)).isTrue();
    }

    @Test
    void crear_conNombreVacio_lanzaInvariantViolation() {
        assertThatThrownBy(() -> Grupo.crear(UUID.randomUUID(), "  ", UUID.randomUUID()))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void crear_conNombreNulo_lanzaInvariantViolation() {
        assertThatThrownBy(() -> Grupo.crear(UUID.randomUUID(), null, UUID.randomUUID()))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void agregarMiembro_usuarioNuevo_seAgregaComoMiembroConRolMiembro() {
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Viaje a Cusco", UUID.randomUUID());
        UUID nuevoUsuario = UUID.randomUUID();

        grupo.agregarMiembro(nuevoUsuario);

        assertThat(grupo.miembros()).hasSize(2);
        assertThat(grupo.tieneMiembro(nuevoUsuario)).isTrue();
        assertThat(grupo.esAdmin(nuevoUsuario)).isFalse();
    }

    @Test
    void agregarMiembro_usuarioYaEsMiembro_lanzaInvariantViolation() {
        UUID creadorId = UUID.randomUUID();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);

        assertThatThrownBy(() -> grupo.agregarMiembro(creadorId))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void agregarMiembro_grupoArchivado_lanzaInvariantViolation() {
        UUID creadorId = UUID.randomUUID();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        grupo.agregarMiembro(UUID.randomUUID());
        grupo.archivar(creadorId);

        assertThatThrownBy(() -> grupo.agregarMiembro(UUID.randomUUID()))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void removerMiembro_actorNoEsAdmin_lanzaUnauthorized() {
        UUID creadorId = UUID.randomUUID();
        UUID miembro2 = UUID.randomUUID();
        UUID miembro3 = UUID.randomUUID();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        grupo.agregarMiembro(miembro2);
        grupo.agregarMiembro(miembro3);

        assertThatThrownBy(() -> grupo.removerMiembro(miembro2, miembro3))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void removerMiembro_usuarioNoEsMiembroDelGrupo_lanzaEntityNotFound() {
        UUID creadorId = UUID.randomUUID();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        grupo.agregarMiembro(UUID.randomUUID());

        assertThatThrownBy(() -> grupo.removerMiembro(creadorId, UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removerMiembro_dejariaMenosDe2Miembros_lanzaInvariantViolation() {
        UUID creadorId = UUID.randomUUID();
        UUID miembro2 = UUID.randomUUID();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        grupo.agregarMiembro(miembro2);

        assertThatThrownBy(() -> grupo.removerMiembro(creadorId, miembro2))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void removerMiembro_conMasDe2Miembros_removeCorrectamente() {
        UUID creadorId = UUID.randomUUID();
        UUID miembro2 = UUID.randomUUID();
        UUID miembro3 = UUID.randomUUID();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        grupo.agregarMiembro(miembro2);
        grupo.agregarMiembro(miembro3);

        grupo.removerMiembro(creadorId, miembro3);

        assertThat(grupo.miembros()).hasSize(2);
        assertThat(grupo.tieneMiembro(miembro3)).isFalse();
    }

    @Test
    void archivar_actorNoEsAdmin_lanzaUnauthorized() {
        UUID creadorId = UUID.randomUUID();
        UUID miembro2 = UUID.randomUUID();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        grupo.agregarMiembro(miembro2);

        assertThatThrownBy(() -> grupo.archivar(miembro2))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void archivar_porAdmin_cambiaEstadoAArchivado() {
        UUID creadorId = UUID.randomUUID();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        grupo.agregarMiembro(UUID.randomUUID());

        grupo.archivar(creadorId);

        assertThat(grupo.estado()).isEqualTo(EstadoGrupo.ARCHIVADO);
    }

    @Test
    void miembros_devuelveListaInmutable() {
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", UUID.randomUUID());

        assertThatThrownBy(() -> grupo.miembros().add(new Miembro(UUID.randomUUID(), Rol.MIEMBRO, Instant.now())))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void reconstruir_reconstruyeSinReaplicarReglasDeCreacion() {
        UUID id = UUID.randomUUID();
        UUID usuario1 = UUID.randomUUID();
        UUID usuario2 = UUID.randomUUID();
        Instant fechaCreacion = Instant.parse("2026-01-01T00:00:00Z");
        List<Miembro> miembros = List.of(
                new Miembro(usuario1, Rol.ADMIN, fechaCreacion),
                new Miembro(usuario2, Rol.MIEMBRO, fechaCreacion)
        );

        Grupo grupo = Grupo.reconstruir(id, "Roomies", fechaCreacion, EstadoGrupo.ARCHIVADO, miembros);

        assertThat(grupo.id()).isEqualTo(id);
        assertThat(grupo.estado()).isEqualTo(EstadoGrupo.ARCHIVADO);
        assertThat(grupo.miembros()).hasSize(2);
    }
}
