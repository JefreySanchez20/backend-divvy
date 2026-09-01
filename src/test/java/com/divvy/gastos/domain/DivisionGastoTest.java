package com.divvy.gastos.domain;

import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DivisionGastoTest {

    private static BigDecimal sumar(DivisionGasto division) {
        return division.detalle().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void crear_igual_montoDivisibleExacto_repartePorIgual() {
        Dinero monto = Dinero.de(new BigDecimal("90.00"), "PEN");
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        UUID u3 = UUID.randomUUID();
        Map<UUID, BigDecimal> participantes = new LinkedHashMap<>();
        participantes.put(u1, BigDecimal.ZERO);
        participantes.put(u2, BigDecimal.ZERO);
        participantes.put(u3, BigDecimal.ZERO);

        DivisionGasto division = DivisionGasto.crear(TipoDivision.IGUAL, monto, participantes);

        assertThat(sumar(division)).isEqualByComparingTo("90.00");
        assertThat(division.detalle().get(u1)).isEqualByComparingTo("30.00");
        assertThat(division.detalle().get(u2)).isEqualByComparingTo("30.00");
        assertThat(division.detalle().get(u3)).isEqualByComparingTo("30.00");
    }

    @Test
    void crear_igual_montoNoDivisibleExacto_sumaExactaYDiferenciaMaximaUnCentavo() {
        Dinero monto = Dinero.de(new BigDecimal("100.00"), "PEN");
        Map<UUID, BigDecimal> participantes = new LinkedHashMap<>();
        participantes.put(UUID.randomUUID(), BigDecimal.ZERO);
        participantes.put(UUID.randomUUID(), BigDecimal.ZERO);
        participantes.put(UUID.randomUUID(), BigDecimal.ZERO);

        DivisionGasto division = DivisionGasto.crear(TipoDivision.IGUAL, monto, participantes);

        assertThat(sumar(division)).isEqualByComparingTo("100.00");
        BigDecimal minimo = division.detalle().values().stream().min(BigDecimal::compareTo).orElseThrow();
        BigDecimal maximo = division.detalle().values().stream().max(BigDecimal::compareTo).orElseThrow();
        assertThat(maximo.subtract(minimo)).isLessThanOrEqualTo(new BigDecimal("0.01"));
    }

    @Test
    void crear_porPorcentaje_sumanCienConRedondeo_sumaExacta() {
        Dinero monto = Dinero.de(new BigDecimal("100.00"), "PEN");
        Map<UUID, BigDecimal> porcentajes = new LinkedHashMap<>();
        porcentajes.put(UUID.randomUUID(), new BigDecimal("33.33"));
        porcentajes.put(UUID.randomUUID(), new BigDecimal("33.33"));
        porcentajes.put(UUID.randomUUID(), new BigDecimal("33.34"));

        DivisionGasto division = DivisionGasto.crear(TipoDivision.PORCENTAJE, monto, porcentajes);

        assertThat(sumar(division)).isEqualByComparingTo("100.00");
    }

    @Test
    void crear_porPorcentaje_noSuman100_lanzaInvariantViolation() {
        Dinero monto = Dinero.de(new BigDecimal("100.00"), "PEN");
        Map<UUID, BigDecimal> porcentajes = new LinkedHashMap<>();
        porcentajes.put(UUID.randomUUID(), new BigDecimal("50"));
        porcentajes.put(UUID.randomUUID(), new BigDecimal("40"));

        assertThatThrownBy(() -> DivisionGasto.crear(TipoDivision.PORCENTAJE, monto, porcentajes))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void crear_porPorcentaje_desigual_calculaMontosProporcionales() {
        Dinero monto = Dinero.de(new BigDecimal("100.00"), "PEN");
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        Map<UUID, BigDecimal> porcentajes = new LinkedHashMap<>();
        porcentajes.put(u1, new BigDecimal("70"));
        porcentajes.put(u2, new BigDecimal("30"));

        DivisionGasto division = DivisionGasto.crear(TipoDivision.PORCENTAJE, monto, porcentajes);

        assertThat(division.detalle().get(u1)).isEqualByComparingTo("70.00");
        assertThat(division.detalle().get(u2)).isEqualByComparingTo("30.00");
    }

    @Test
    void crear_montoFijo_sumaExacta_seCreaCorrectamente() {
        Dinero monto = Dinero.de(new BigDecimal("100.00"), "PEN");
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        Map<UUID, BigDecimal> montos = new LinkedHashMap<>();
        montos.put(u1, new BigDecimal("60.00"));
        montos.put(u2, new BigDecimal("40.00"));

        DivisionGasto division = DivisionGasto.crear(TipoDivision.MONTO_FIJO, monto, montos);

        assertThat(sumar(division)).isEqualByComparingTo("100.00");
    }

    @Test
    void crear_montoFijo_sumaNoCoincide_lanzaInvariantViolation() {
        Dinero monto = Dinero.de(new BigDecimal("100.00"), "PEN");
        Map<UUID, BigDecimal> montos = new LinkedHashMap<>();
        montos.put(UUID.randomUUID(), new BigDecimal("60.00"));
        montos.put(UUID.randomUUID(), new BigDecimal("30.00"));

        assertThatThrownBy(() -> DivisionGasto.crear(TipoDivision.MONTO_FIJO, monto, montos))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void crear_detalleVacio_lanzaInvariantViolation() {
        Dinero monto = Dinero.de(new BigDecimal("100.00"), "PEN");

        assertThatThrownBy(() -> DivisionGasto.crear(TipoDivision.MONTO_FIJO, monto, Map.of()))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void reconstruir_noReaplicaValidaciones() {
        UUID u1 = UUID.randomUUID();
        Map<UUID, BigDecimal> detalle = Map.of(u1, new BigDecimal("999.00"));

        DivisionGasto division = DivisionGasto.reconstruir(TipoDivision.MONTO_FIJO, detalle);

        assertThat(division.detalle()).isEqualTo(detalle);
    }
}
