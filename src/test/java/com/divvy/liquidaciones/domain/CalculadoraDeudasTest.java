package com.divvy.liquidaciones.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraDeudasTest {

    private final CalculadoraDeudas calculadora = new CalculadoraDeudas();

    private void verificarLiquidaCorrectamente(Map<UUID, BigDecimal> balances, List<TransaccionSugerida> transacciones) {
        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {
            UUID usuarioId = entry.getKey();
            BigDecimal balance = entry.getValue();

            BigDecimal totalPagado = transacciones.stream()
                    .filter(t -> t.deudorId().equals(usuarioId))
                    .map(TransaccionSugerida::monto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRecibido = transacciones.stream()
                    .filter(t -> t.acreedorId().equals(usuarioId))
                    .map(TransaccionSugerida::monto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            assertThat(totalRecibido.subtract(totalPagado)).isEqualByComparingTo(balance);
        }

        for (TransaccionSugerida t : transacciones) {
            assertThat(t.deudorId()).isNotEqualTo(t.acreedorId());
            assertThat(t.monto()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Test
    void calcular_dosMiembros_generaUnaTransaccion() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Map<UUID, BigDecimal> balances = Map.of(a, new BigDecimal("50.00"), b, new BigDecimal("-50.00"));

        List<TransaccionSugerida> transacciones = calculadora.calcular(balances);

        assertThat(transacciones).hasSize(1);
        verificarLiquidaCorrectamente(balances, transacciones);
    }

    @Test
    void calcular_tresMiembros_casoClasicoSplitwise() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        Map<UUID, BigDecimal> balances = Map.of(
                a, new BigDecimal("60.00"),
                b, new BigDecimal("-30.00"),
                c, new BigDecimal("-30.00")
        );

        List<TransaccionSugerida> transacciones = calculadora.calcular(balances);

        assertThat(transacciones).hasSize(2);
        verificarLiquidaCorrectamente(balances, transacciones);
    }

    @Test
    void calcular_cincoMiembros_minimizaTransacciones() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        UUID d = UUID.randomUUID();
        UUID e = UUID.randomUUID();
        Map<UUID, BigDecimal> balances = new LinkedHashMap<>();
        balances.put(a, new BigDecimal("100.00"));
        balances.put(b, new BigDecimal("50.00"));
        balances.put(c, new BigDecimal("-30.00"));
        balances.put(d, new BigDecimal("-70.00"));
        balances.put(e, new BigDecimal("-50.00"));

        List<TransaccionSugerida> transacciones = calculadora.calcular(balances);

        assertThat(transacciones).hasSize(3);
        verificarLiquidaCorrectamente(balances, transacciones);
    }

    @Test
    void calcular_conCentavosQueNoDividenParejo_liquidaExacto() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        Map<UUID, BigDecimal> balances = Map.of(
                a, new BigDecimal("33.34"),
                b, new BigDecimal("-16.67"),
                c, new BigDecimal("-16.67")
        );

        List<TransaccionSugerida> transacciones = calculadora.calcular(balances);

        verificarLiquidaCorrectamente(balances, transacciones);
    }

    @Test
    void calcular_unDeudorConMultiplesAcreedores_liquidaTodasLasDeudas() {
        UUID deudor = UUID.randomUUID();
        UUID acreedor1 = UUID.randomUUID();
        UUID acreedor2 = UUID.randomUUID();
        Map<UUID, BigDecimal> balances = Map.of(
                deudor, new BigDecimal("-100.00"),
                acreedor1, new BigDecimal("60.00"),
                acreedor2, new BigDecimal("40.00")
        );

        List<TransaccionSugerida> transacciones = calculadora.calcular(balances);

        assertThat(transacciones).hasSize(2);
        verificarLiquidaCorrectamente(balances, transacciones);
    }

    @Test
    void calcular_todosLosBalancesEnCero_noGeneraTransacciones() {
        Map<UUID, BigDecimal> balances = Map.of(
                UUID.randomUUID(), BigDecimal.ZERO,
                UUID.randomUUID(), BigDecimal.ZERO
        );

        assertThat(calculadora.calcular(balances)).isEmpty();
    }

    @Test
    void calcular_mapaVacio_noGeneraTransacciones() {
        assertThat(calculadora.calcular(Map.of())).isEmpty();
    }

    @Test
    void calcular_balancesQueNoSumanCero_lanzaIllegalState() {
        Map<UUID, BigDecimal> balances = Map.of(
                UUID.randomUUID(), new BigDecimal("50.00"),
                UUID.randomUUID(), new BigDecimal("-30.00")
        );

        assertThatThrownBy(() -> calculadora.calcular(balances))
                .isInstanceOf(IllegalStateException.class);
    }
}
