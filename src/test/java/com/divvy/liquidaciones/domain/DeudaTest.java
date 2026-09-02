package com.divvy.liquidaciones.domain;

import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeudaTest {

    @Test
    void crear_datosValidos_creaDeudaPendiente() {
        UUID deudorId = UUID.randomUUID();
        UUID acreedorId = UUID.randomUUID();
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");

        Deuda deuda = Deuda.crear(UUID.randomUUID(), deudorId, acreedorId, monto);

        assertThat(deuda.deudorId()).isEqualTo(deudorId);
        assertThat(deuda.acreedorId()).isEqualTo(acreedorId);
        assertThat(deuda.estado()).isEqualTo(EstadoDeuda.PENDIENTE);
        assertThat(deuda.fechaPago()).isNull();
    }

    @Test
    void crear_montoCero_lanzaInvariantViolation() {
        Dinero monto = Dinero.de(BigDecimal.ZERO, "PEN");

        assertThatThrownBy(() -> Deuda.crear(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), monto))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void crear_deudorIgualAcreedor_lanzaInvariantViolation() {
        UUID usuario = UUID.randomUUID();
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");

        assertThatThrownBy(() -> Deuda.crear(UUID.randomUUID(), usuario, usuario, monto))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void marcarComoPagada_deudaPendiente_laMarcaConFecha() {
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");
        Deuda deuda = Deuda.crear(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), monto);

        deuda.marcarComoPagada();

        assertThat(deuda.estado()).isEqualTo(EstadoDeuda.PAGADA);
        assertThat(deuda.fechaPago()).isNotNull();
    }

    @Test
    void marcarComoPagada_deudaYaPagada_lanzaInvariantViolation() {
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");
        Deuda deuda = Deuda.crear(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), monto);
        deuda.marcarComoPagada();

        assertThatThrownBy(deuda::marcarComoPagada).isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void reconstruir_reconstruyeSinReaplicarValidaciones() {
        UUID id = UUID.randomUUID();
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");
        Instant fechaPago = Instant.now();

        Deuda deuda = Deuda.reconstruir(id, UUID.randomUUID(), UUID.randomUUID(), monto, EstadoDeuda.PAGADA, fechaPago);

        assertThat(deuda.id()).isEqualTo(id);
        assertThat(deuda.estado()).isEqualTo(EstadoDeuda.PAGADA);
        assertThat(deuda.fechaPago()).isEqualTo(fechaPago);
    }
}
