package com.divvy.gastos.domain;

import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GastoTest {

    private DivisionGasto divisionValida(Dinero monto, UUID... participantes) {
        Map<UUID, BigDecimal> detalle = new java.util.LinkedHashMap<>();
        for (UUID p : participantes) {
            detalle.put(p, BigDecimal.ZERO);
        }
        return DivisionGasto.crear(TipoDivision.IGUAL, monto, detalle);
    }

    @Test
    void registrar_datosValidos_creaGasto() {
        UUID grupoId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto division = divisionValida(monto, pagadoPor, UUID.randomUUID());

        Gasto gasto = Gasto.registrar(
                UUID.randomUUID(), grupoId, "Cena", monto, pagadoPor, Instant.now(), "Comida", division);

        assertThat(gasto.grupoId()).isEqualTo(grupoId);
        assertThat(gasto.descripcion()).isEqualTo("Cena");
        assertThat(gasto.monto()).isEqualTo(monto);
        assertThat(gasto.pagadoPor()).isEqualTo(pagadoPor);
    }

    @Test
    void registrar_descripcionVacia_lanzaInvariantViolation() {
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        UUID pagadoPor = UUID.randomUUID();
        DivisionGasto division = divisionValida(monto, pagadoPor);

        assertThatThrownBy(() -> Gasto.registrar(
                UUID.randomUUID(), UUID.randomUUID(), " ", monto, pagadoPor, Instant.now(), "Comida", division))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void registrar_montoCero_lanzaInvariantViolation() {
        Dinero monto = Dinero.de(BigDecimal.ZERO, "PEN");
        UUID pagadoPor = UUID.randomUUID();
        DivisionGasto division = DivisionGasto.reconstruir(TipoDivision.MONTO_FIJO, Map.of(pagadoPor, BigDecimal.ZERO));

        assertThatThrownBy(() -> Gasto.registrar(
                UUID.randomUUID(), UUID.randomUUID(), "Cena", monto, pagadoPor, Instant.now(), "Comida", division))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void registrar_montoNegativo_lanzaInvariantViolation() {
        Dinero monto = Dinero.de(new BigDecimal("-10.00"), "PEN");
        UUID pagadoPor = UUID.randomUUID();
        DivisionGasto division = DivisionGasto.reconstruir(TipoDivision.MONTO_FIJO, Map.of(pagadoPor, new BigDecimal("-10.00")));

        assertThatThrownBy(() -> Gasto.registrar(
                UUID.randomUUID(), UUID.randomUUID(), "Cena", monto, pagadoPor, Instant.now(), "Comida", division))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void registrar_divisionNoCoincideConMonto_lanzaInvariantViolation() {
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        UUID pagadoPor = UUID.randomUUID();
        DivisionGasto divisionQueNoCoincide = DivisionGasto.reconstruir(
                TipoDivision.MONTO_FIJO, Map.of(pagadoPor, new BigDecimal("30.00")));

        assertThatThrownBy(() -> Gasto.registrar(
                UUID.randomUUID(), UUID.randomUUID(), "Cena", monto, pagadoPor, Instant.now(), "Comida", divisionQueNoCoincide))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void editar_datosValidos_actualizaElGasto() {
        UUID pagadoPor = UUID.randomUUID();
        Dinero montoOriginal = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto divisionOriginal = divisionValida(montoOriginal, pagadoPor);
        Gasto gasto = Gasto.registrar(
                UUID.randomUUID(), UUID.randomUUID(), "Cena", montoOriginal, pagadoPor, Instant.now(), "Comida", divisionOriginal);

        Dinero montoNuevo = Dinero.de(new BigDecimal("80.00"), "PEN");
        DivisionGasto divisionNueva = divisionValida(montoNuevo, pagadoPor);
        gasto.editar("Cena actualizada", montoNuevo, pagadoPor, Instant.now(), "Restaurante", divisionNueva);

        assertThat(gasto.descripcion()).isEqualTo("Cena actualizada");
        assertThat(gasto.monto()).isEqualTo(montoNuevo);
        assertThat(gasto.categoria()).isEqualTo("Restaurante");
    }

    @Test
    void editar_divisionNoCoincideConMonto_lanzaInvariantViolationYNoMutaElGasto() {
        UUID pagadoPor = UUID.randomUUID();
        Dinero montoOriginal = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto divisionOriginal = divisionValida(montoOriginal, pagadoPor);
        Gasto gasto = Gasto.registrar(
                UUID.randomUUID(), UUID.randomUUID(), "Cena", montoOriginal, pagadoPor, Instant.now(), "Comida", divisionOriginal);

        Dinero montoNuevo = Dinero.de(new BigDecimal("80.00"), "PEN");
        DivisionGasto divisionQueNoCoincide = DivisionGasto.reconstruir(
                TipoDivision.MONTO_FIJO, Map.of(pagadoPor, new BigDecimal("10.00")));

        assertThatThrownBy(() -> gasto.editar("Cena", montoNuevo, pagadoPor, Instant.now(), "Comida", divisionQueNoCoincide))
                .isInstanceOf(InvariantViolationException.class);
        assertThat(gasto.monto()).isEqualTo(montoOriginal);
    }
}
