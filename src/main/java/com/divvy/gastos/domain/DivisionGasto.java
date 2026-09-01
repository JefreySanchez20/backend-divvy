package com.divvy.gastos.domain;

import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.InvariantViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DivisionGasto {

    private final TipoDivision tipo;
    private final Map<UUID, BigDecimal> detalle;

    private DivisionGasto(TipoDivision tipo, Map<UUID, BigDecimal> detalle) {
        this.tipo = tipo;
        this.detalle = detalle;
    }

    public static DivisionGasto crear(TipoDivision tipo, Dinero montoTotal, Map<UUID, BigDecimal> detalle) {
        Objects.requireNonNull(tipo, "El tipo de división no puede ser nulo");
        Objects.requireNonNull(montoTotal, "El monto total no puede ser nulo");
        validarDetalleNoVacio(detalle);

        return switch (tipo) {
            case IGUAL -> crearIgual(montoTotal, detalle.keySet());
            case PORCENTAJE -> crearPorPorcentaje(montoTotal, detalle);
            case MONTO_FIJO -> crearMontoFijo(montoTotal, detalle);
        };
    }

    public static DivisionGasto reconstruir(TipoDivision tipo, Map<UUID, BigDecimal> detalle) {
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(detalle);
        return new DivisionGasto(tipo, new LinkedHashMap<>(detalle));
    }

    private static void validarDetalleNoVacio(Map<UUID, BigDecimal> detalle) {
        if (detalle == null || detalle.isEmpty()) {
            throw new InvariantViolationException("La división del gasto debe tener al menos un participante");
        }
    }

    private static DivisionGasto crearIgual(Dinero montoTotal, java.util.Set<UUID> participantes) {
        Map<UUID, BigDecimal> montos = repartirEntre(montoTotal.monto(), participantes.stream().toList());
        return new DivisionGasto(TipoDivision.IGUAL, montos);
    }

    private static DivisionGasto crearPorPorcentaje(Dinero montoTotal, Map<UUID, BigDecimal> porcentajes) {
        BigDecimal sumaPorcentajes = porcentajes.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumaPorcentajes.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new InvariantViolationException("Los porcentajes de la división deben sumar exactamente 100");
        }

        int scale = montoTotal.monto().scale();
        List<UUID> idsOrdenados = porcentajes.keySet().stream().sorted().toList();
        Map<UUID, BigDecimal> montosBase = new LinkedHashMap<>();
        for (UUID id : idsOrdenados) {
            BigDecimal porcentaje = porcentajes.get(id);
            BigDecimal montoBruto = montoTotal.monto().multiply(porcentaje)
                    .divide(BigDecimal.valueOf(100), scale, RoundingMode.DOWN);
            montosBase.put(id, montoBruto);
        }
        return new DivisionGasto(TipoDivision.PORCENTAJE, ajustarRedondeo(montoTotal.monto(), montosBase, idsOrdenados, scale));
    }

    private static DivisionGasto crearMontoFijo(Dinero montoTotal, Map<UUID, BigDecimal> montos) {
        validarSumaExacta(montoTotal.monto(), montos);
        return new DivisionGasto(TipoDivision.MONTO_FIJO, new LinkedHashMap<>(montos));
    }

    private static Map<UUID, BigDecimal> repartirEntre(BigDecimal montoTotal, List<UUID> participantes) {
        int scale = montoTotal.scale();
        BigDecimal cantidad = BigDecimal.valueOf(participantes.size());
        BigDecimal montoBase = montoTotal.divide(cantidad, scale, RoundingMode.DOWN);

        List<UUID> idsOrdenados = participantes.stream().sorted().toList();
        Map<UUID, BigDecimal> montosBase = new LinkedHashMap<>();
        for (UUID id : idsOrdenados) {
            montosBase.put(id, montoBase);
        }
        return ajustarRedondeo(montoTotal, montosBase, idsOrdenados, scale);
    }

    private static Map<UUID, BigDecimal> ajustarRedondeo(BigDecimal montoTotal, Map<UUID, BigDecimal> montosBase, List<UUID> idsOrdenados, int scale) {
        BigDecimal sumaBase = montosBase.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal restante = montoTotal.subtract(sumaBase);
        BigDecimal unidad = BigDecimal.ONE.movePointLeft(scale);

        int centavosExtra = restante.divide(unidad, 0, RoundingMode.HALF_UP).intValueExact();

        Map<UUID, BigDecimal> resultado = new LinkedHashMap<>();
        for (int i = 0; i < idsOrdenados.size(); i++) {
            UUID id = idsOrdenados.get(i);
            BigDecimal monto = montosBase.get(id);
            if (i < centavosExtra) {
                monto = monto.add(unidad);
            }
            resultado.put(id, monto);
        }
        return resultado;
    }

    private static void validarSumaExacta(BigDecimal montoTotal, Map<UUID, BigDecimal> montos) {
        BigDecimal suma = montos.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (suma.compareTo(montoTotal) != 0) {
            throw new InvariantViolationException(
                    "La suma de las divisiones (" + suma + ") no coincide con el monto total del gasto (" + montoTotal + ")");
        }
    }

    public TipoDivision tipo() {
        return tipo;
    }

    public Map<UUID, BigDecimal> detalle() {
        return Map.copyOf(detalle);
    }
}
