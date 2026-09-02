package com.divvy.liquidaciones.infrastructure.persistence;

import com.divvy.TestcontainersConfiguration;
import com.divvy.liquidaciones.domain.Deuda;
import com.divvy.liquidaciones.domain.EstadoDeuda;
import com.divvy.liquidaciones.domain.Liquidacion;
import com.divvy.shared.domain.Dinero;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class LiquidacionRepositoryImplIT {

    @Autowired
    private LiquidacionJpaRepository liquidacionJpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private LiquidacionRepositoryImpl repository;
    private LectorBalanceGrupoImpl lectorBalanceGrupo;

    @BeforeEach
    void setUp() {
        repository = new LiquidacionRepositoryImpl(liquidacionJpaRepository);
        lectorBalanceGrupo = new LectorBalanceGrupoImpl(jdbcTemplate);
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
        jdbcTemplate.update("INSERT INTO grupos (id, nombre, estado) VALUES (?, ?, 'ACTIVO')", id, "Grupo de prueba");
        return id;
    }

    private void crearGasto(UUID grupoId, UUID pagadoPor, BigDecimal monto, String moneda, Map<UUID, BigDecimal> detalle) {
        UUID gastoId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO gastos (id, grupo_id, descripcion, monto, moneda, pagado_por, fecha, categoria, tipo_division) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'IGUAL')",
                gastoId, grupoId, "Gasto de prueba", monto, moneda, pagadoPor, Timestamp.from(Instant.now()), "Otros"
        );
        for (Map.Entry<UUID, BigDecimal> entry : detalle.entrySet()) {
            jdbcTemplate.update(
                    "INSERT INTO gasto_division_detalle (gasto_id, usuario_id, valor) VALUES (?, ?, ?)",
                    gastoId, entry.getKey(), entry.getValue()
            );
        }
    }

    @Test
    void guardarYBuscarPorId_persisteLaLiquidacionConSusDeudas() {
        UUID grupoId = crearGrupo();
        UUID deudorId = crearUsuario();
        UUID acreedorId = crearUsuario();
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");
        Deuda deuda = Deuda.crear(UUID.randomUUID(), deudorId, acreedorId, monto);
        Liquidacion liquidacion = Liquidacion.calcular(UUID.randomUUID(), grupoId, List.of(deuda));

        repository.guardar(liquidacion);

        Optional<Liquidacion> recuperada = repository.buscarPorId(liquidacion.id());

        assertThat(recuperada).isPresent();
        assertThat(recuperada.get().deudas()).hasSize(1);
        assertThat(recuperada.get().deudas().get(0).monto().monto()).isEqualByComparingTo("30.00");
        assertThat(recuperada.get().deudas().get(0).estado()).isEqualTo(EstadoDeuda.PENDIENTE);
    }

    @Test
    void marcarComoPagadaYGuardar_actualizaElEstadoPersistido() {
        UUID grupoId = crearGrupo();
        UUID deudorId = crearUsuario();
        UUID acreedorId = crearUsuario();
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");
        Deuda deuda = Deuda.crear(UUID.randomUUID(), deudorId, acreedorId, monto);
        Liquidacion liquidacion = Liquidacion.calcular(UUID.randomUUID(), grupoId, List.of(deuda));
        repository.guardar(liquidacion);

        Liquidacion recuperada = repository.buscarPorId(liquidacion.id()).orElseThrow();
        recuperada.buscarDeuda(deuda.id()).orElseThrow().marcarComoPagada();
        repository.guardar(recuperada);

        Liquidacion actualizada = repository.buscarPorId(liquidacion.id()).orElseThrow();
        Deuda deudaActualizada = actualizada.buscarDeuda(deuda.id()).orElseThrow();
        assertThat(deudaActualizada.estado()).isEqualTo(EstadoDeuda.PAGADA);
        assertThat(deudaActualizada.fechaPago()).isNotNull();
    }

    @Test
    void buscarPorGrupo_devuelveSoloLasLiquidacionesDeEseGrupo() {
        UUID grupoId = crearGrupo();
        UUID otroGrupoId = crearGrupo();
        UUID deudorId = crearUsuario();
        UUID acreedorId = crearUsuario();
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");

        repository.guardar(Liquidacion.calcular(UUID.randomUUID(), grupoId,
                List.of(Deuda.crear(UUID.randomUUID(), deudorId, acreedorId, monto))));
        repository.guardar(Liquidacion.calcular(UUID.randomUUID(), otroGrupoId,
                List.of(Deuda.crear(UUID.randomUUID(), deudorId, acreedorId, monto))));

        List<Liquidacion> historial = repository.buscarPorGrupo(grupoId);

        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).grupoId()).isEqualTo(grupoId);
    }

    @Test
    void lectorBalanceGrupo_calculaBalanceCorrectoDesdeGastosReales() {
        UUID grupoId = crearGrupo();
        UUID ana = crearUsuario();
        UUID beto = crearUsuario();

        crearGasto(grupoId, ana, new BigDecimal("100.00"), "PEN",
                Map.of(ana, new BigDecimal("50.00"), beto, new BigDecimal("50.00")));

        Map<String, Map<UUID, BigDecimal>> balances = lectorBalanceGrupo.obtenerBalances(grupoId);

        assertThat(balances).containsKey("PEN");
        assertThat(balances.get("PEN").get(ana)).isEqualByComparingTo("50.00");
        assertThat(balances.get("PEN").get(beto)).isEqualByComparingTo("-50.00");
    }

    @Test
    void lectorBalanceGrupo_variosGastos_acumulaCorrectamente() {
        UUID grupoId = crearGrupo();
        UUID ana = crearUsuario();
        UUID beto = crearUsuario();

        crearGasto(grupoId, ana, new BigDecimal("100.00"), "PEN",
                Map.of(ana, new BigDecimal("50.00"), beto, new BigDecimal("50.00")));
        crearGasto(grupoId, beto, new BigDecimal("40.00"), "PEN",
                Map.of(ana, new BigDecimal("20.00"), beto, new BigDecimal("20.00")));

        Map<String, Map<UUID, BigDecimal>> balances = lectorBalanceGrupo.obtenerBalances(grupoId);

        assertThat(balances.get("PEN").get(ana)).isEqualByComparingTo("30.00");
        assertThat(balances.get("PEN").get(beto)).isEqualByComparingTo("-30.00");
    }

    @Test
    void lectorBalanceGrupo_sinGastos_devuelveMapaVacio() {
        UUID grupoId = crearGrupo();

        assertThat(lectorBalanceGrupo.obtenerBalances(grupoId)).isEmpty();
    }
}
