package com.divvy.grupos.infrastructure.persistence;

import com.divvy.TestcontainersConfiguration;
import com.divvy.grupos.domain.EstadoGrupo;
import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class GrupoRepositoryImplIT {

    @Autowired
    private GrupoJpaRepository grupoJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private GrupoRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new GrupoRepositoryImpl(grupoJpaRepository);
    }

    private UUID crearUsuario() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO usuarios (id, email, password_hash, nombre) VALUES (?, ?, ?, ?)",
                id, id + "@test.com", "hash", "Usuario de prueba"
        );
        return id;
    }

    @Test
    void guardarYBuscarPorId_persisteElGrupoConSusMiembros() {
        UUID creadorId = crearUsuario();
        UUID segundoMiembro = crearUsuario();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        grupo.agregarMiembro(segundoMiembro);

        repository.guardar(grupo);

        Optional<Grupo> recuperado = repository.buscarPorId(grupo.id());

        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().nombre()).isEqualTo("Roomies");
        assertThat(recuperado.get().estado()).isEqualTo(EstadoGrupo.ACTIVO);
        assertThat(recuperado.get().miembros()).hasSize(2);
        assertThat(recuperado.get().esAdmin(creadorId)).isTrue();
    }

    @Test
    void buscarPorId_noExiste_devuelveOptionalVacio() {
        assertThat(repository.buscarPorId(UUID.randomUUID())).isEmpty();
    }

    @Test
    void buscarPorUsuario_devuelveSoloLosGruposDondeEsMiembro() {
        UUID usuarioA = crearUsuario();
        UUID usuarioB = crearUsuario();

        Grupo grupoDeA = Grupo.crear(UUID.randomUUID(), "Grupo de A", usuarioA);
        Grupo grupoDeB = Grupo.crear(UUID.randomUUID(), "Grupo de B", usuarioB);
        repository.guardar(grupoDeA);
        repository.guardar(grupoDeB);

        List<Grupo> gruposDeA = repository.buscarPorUsuario(usuarioA);

        assertThat(gruposDeA).hasSize(1);
        assertThat(gruposDeA.get(0).nombre()).isEqualTo("Grupo de A");
    }

    @Test
    void guardar_actualizaGrupoExistenteTrasArchivar() {
        UUID creadorId = crearUsuario();
        UUID segundoMiembro = crearUsuario();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        grupo.agregarMiembro(segundoMiembro);
        repository.guardar(grupo);

        Grupo recuperado = repository.buscarPorId(grupo.id()).orElseThrow();
        recuperado.archivar(creadorId);
        repository.guardar(recuperado);

        Grupo actualizado = repository.buscarPorId(grupo.id()).orElseThrow();
        assertThat(actualizado.estado()).isEqualTo(EstadoGrupo.ARCHIVADO);
    }

    @Test
    void miembroPersisteConElRolCorrecto() {
        UUID creadorId = crearUsuario();
        Grupo grupo = Grupo.crear(UUID.randomUUID(), "Roomies", creadorId);
        repository.guardar(grupo);

        Grupo recuperado = repository.buscarPorId(grupo.id()).orElseThrow();

        assertThat(recuperado.miembros()).hasSize(1);
        assertThat(recuperado.miembros().get(0).rol()).isEqualTo(Rol.ADMIN);
        assertThat(recuperado.miembros().get(0).usuarioId()).isEqualTo(creadorId);
    }
}
