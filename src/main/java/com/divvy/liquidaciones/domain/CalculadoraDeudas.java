package com.divvy.liquidaciones.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CalculadoraDeudas {

    public List<TransaccionSugerida> calcular(Map<UUID, BigDecimal> balances) {
        validarBalanceCero(balances);

        List<Saldo> deudores = new ArrayList<>();
        List<Saldo> acreedores = new ArrayList<>();

        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {
            int comparacion = entry.getValue().compareTo(BigDecimal.ZERO);
            if (comparacion < 0) {
                deudores.add(new Saldo(entry.getKey(), entry.getValue().abs()));
            } else if (comparacion > 0) {
                acreedores.add(new Saldo(entry.getKey(), entry.getValue()));
            }
        }

        List<TransaccionSugerida> transacciones = new ArrayList<>();

        while (!deudores.isEmpty() && !acreedores.isEmpty()) {
            Saldo mayorDeudor = mayor(deudores);
            Saldo mayorAcreedor = mayor(acreedores);

            BigDecimal monto = mayorDeudor.monto.min(mayorAcreedor.monto);
            transacciones.add(new TransaccionSugerida(mayorDeudor.usuarioId, mayorAcreedor.usuarioId, monto));

            mayorDeudor.monto = mayorDeudor.monto.subtract(monto);
            mayorAcreedor.monto = mayorAcreedor.monto.subtract(monto);

            if (mayorDeudor.monto.compareTo(BigDecimal.ZERO) == 0) {
                deudores.remove(mayorDeudor);
            }
            if (mayorAcreedor.monto.compareTo(BigDecimal.ZERO) == 0) {
                acreedores.remove(mayorAcreedor);
            }
        }

        return transacciones;
    }

    private static void validarBalanceCero(Map<UUID, BigDecimal> balances) {
        BigDecimal suma = balances.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (suma.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Los balances del grupo no suman cero, diferencia: " + suma);
        }
    }

    private static Saldo mayor(List<Saldo> saldos) {
        return saldos.stream().max(Comparator.comparing(s -> s.monto)).orElseThrow();
    }

    private static final class Saldo {
        private final UUID usuarioId;
        private BigDecimal monto;

        private Saldo(UUID usuarioId, BigDecimal monto) {
            this.usuarioId = usuarioId;
            this.monto = monto;
        }
    }
}
