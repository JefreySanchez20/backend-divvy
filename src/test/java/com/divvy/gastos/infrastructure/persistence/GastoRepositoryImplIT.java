package com.divvy.gastos.infrastructure.persistence;

import com.divvy.TestcontainersConfiguration;
import com.divvy.gastos.domain.DivisionGasto;
import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.TipoDivision;
import com.divvy.shared.domain.Dinero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class GastoRepositoryImplIT {

    @Autowired
    private GastoJpaRepository gastoJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private GastoRepositoryImpl repository;
    private UUID grupoId;
    private UUID usuario1;
    private UUID usuario2;

    @BeforeEach
    void setUp() {
        repository = new GastoRepositoryImpl(gastoJpaRepository);
        usuario1 = crearUsuario();
        usuario2 = crearUsuario();
        grupoId = crearGrupo();
        agregarMiembro(grupoId, usuario1, "ADMIN");
        agregarMiembro(grupoId, usuario2, "MIEMBRO");
    }

    private UUID crearUsuario() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO usuarios (id, email, password_hash, nombre) VALUES (?, ?, ?, ?)",
                id, id + "@test.com", "hash", "Usuario de prueba"
        );
        return id;
    }

    private UUID crearGrupo() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO grupos (id, nombre, estado) VALUES (?, ?, 'ACTIVO')", id, "Roomies");
        return id;
    }

    private void agregarMiembro(UUID grupoId, UUID usuarioId, String rol) {
        jdbcTemplate.update(
                "INSERT INTO grupo_miembros (grupo_id, usuario_id, rol) VALUES (?, ?, ?)",
                grupoId, usuarioId, rol
        );
    }

    @Test
    void guardarYBuscarPorId_persisteElGastoConSuDivision() {
        Dinero monto = Dinero.de(new BigDecimal("100.00"), "PEN");
        DivisionGasto division = DivisionGasto.crear(TipoDivision.IGUAL, monto, Map.of(usuario1, BigDecimal.ZERO, usuario2, BigDecimal.ZERO));
        Gasto gasto = Gasto.registrar(UUID.randomUUID(), grupoId, "Cena", monto, usuario1, Instant.now(), "Comida", division);

        repository.guardar(gasto);

        Optional<Gasto> recuperado = repository.buscarPorId(gasto.id());

        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().descripcion()).isEqualTo("Cena");
        assertThat(recuperado.get().monto().monto()).isEqualByComparingTo("100.00");
        assertThat(recuperado.get().division().detalle()).hasSize(2);
        BigDecimal suma = recuperado.get().division().detalle().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(suma).isEqualByComparingTo("100.00");
    }

    @Test
    void buscarPorGrupo_devuelveSoloLosGastosDeEseGrupo() {
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto division = DivisionGasto.crear(TipoDivision.MONTO_FIJO, monto, Map.of(usuario1, new BigDecimal("50.00")));
        Gasto gasto1 = Gasto.registrar(UUID.randomUUID(), grupoId, "Cena", monto, usuario1, Instant.now(), "Comida", division);
        Gasto gasto2 = Gasto.registrar(UUID.randomUUID(), grupoId, "Taxi", monto, usuario1, Instant.now(), "Transporte", division);
        repository.guardar(gasto1);
        repository.guardar(gasto2);

        List<Gasto> gastos = repository.buscarPorGrupo(grupoId);

        assertThat(gastos).hasSize(2);
    }

    @Test
    void eliminar_borraElGastoYSuDivision() {
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto division = DivisionGasto.crear(TipoDivision.MONTO_FIJO, monto, Map.of(usuario1, new BigDecimal("50.00")));
        Gasto gasto = Gasto.registrar(UUID.randomUUID(), grupoId, "Cena", monto, usuario1, Instant.now(), "Comida", division);
        repository.guardar(gasto);

        repository.eliminar(gasto.id());

        assertThat(repository.buscarPorId(gasto.id())).isEmpty();
    }

    @Test
    void verificadorMiembroGrupo_miembroActivo_devuelveTrue() {
        VerificadorMiembroGrupoImpl verificador = new VerificadorMiembroGrupoImpl(jdbcTemplate);

        assertThat(verificador.esMiembroActivo(grupoId, usuario1)).isTrue();
    }

    @Test
    void verificadorMiembroGrupo_usuarioNoEsMiembro_devuelveFalse() {
        VerificadorMiembroGrupoImpl verificador = new VerificadorMiembroGrupoImpl(jdbcTemplate);

        assertThat(verificador.esMiembroActivo(grupoId, UUID.randomUUID())).isFalse();
    }

    @Test
    void verificadorMiembroGrupo_grupoArchivado_devuelveFalse() {
        jdbcTemplate.update("UPDATE grupos SET estado = 'ARCHIVADO' WHERE id = ?", grupoId);
        VerificadorMiembroGrupoImpl verificador = new VerificadorMiembroGrupoImpl(jdbcTemplate);

        assertThat(verificador.esMiembroActivo(grupoId, usuario1)).isFalse();
    }
}
