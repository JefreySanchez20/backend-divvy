package com.divvy.liquidaciones.domain;

import com.divvy.shared.domain.Dinero;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LiquidacionTest {

    @Test
    void calcular_creaLiquidacionConFechaActual() {
        UUID grupoId = UUID.randomUUID();
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");
        Deuda deuda = Deuda.crear(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), monto);

        Liquidacion liquidacion = Liquidacion.calcular(UUID.randomUUID(), grupoId, List.of(deuda));

        assertThat(liquidacion.grupoId()).isEqualTo(grupoId);
        assertThat(liquidacion.deudas()).hasSize(1);
        assertThat(liquidacion.fechaCalculo()).isNotNull();
    }

    @Test
    void buscarDeuda_existente_laEncuentra() {
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");
        Deuda deuda = Deuda.crear(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), monto);
        Liquidacion liquidacion = Liquidacion.calcular(UUID.randomUUID(), UUID.randomUUID(), List.of(deuda));

        assertThat(liquidacion.buscarDeuda(deuda.id())).isPresent();
    }

    @Test
    void buscarDeuda_inexistente_devuelveVacio() {
        Liquidacion liquidacion = Liquidacion.calcular(UUID.randomUUID(), UUID.randomUUID(), List.of());

        assertThat(liquidacion.buscarDeuda(UUID.randomUUID())).isEmpty();
    }

    @Test
    void reconstruir_reconstruyeConFechaDada() {
        UUID id = UUID.randomUUID();
        Instant fecha = Instant.parse("2026-01-01T00:00:00Z");

        Liquidacion liquidacion = Liquidacion.reconstruir(id, UUID.randomUUID(), fecha, List.of());

        assertThat(liquidacion.id()).isEqualTo(id);
        assertThat(liquidacion.fechaCalculo()).isEqualTo(fecha);
    }
}
