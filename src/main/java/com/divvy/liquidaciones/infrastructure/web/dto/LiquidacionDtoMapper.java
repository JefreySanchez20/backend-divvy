package com.divvy.liquidaciones.infrastructure.web.dto;

import com.divvy.liquidaciones.domain.Deuda;
import com.divvy.liquidaciones.domain.EstadoDeuda;
import com.divvy.liquidaciones.domain.Liquidacion;

import java.util.List;

public final class LiquidacionDtoMapper {

    private LiquidacionDtoMapper() {
    }

    public static LiquidacionResponse toResponse(Liquidacion liquidacion) {
        List<DeudaResponse> deudas = liquidacion.deudas().stream()
                .map(LiquidacionDtoMapper::toResponse)
                .toList();
        return new LiquidacionResponse(liquidacion.id(), liquidacion.grupoId(), liquidacion.fechaCalculo(), deudas);
    }

    private static DeudaResponse toResponse(Deuda deuda) {
        return new DeudaResponse(
                deuda.id(),
                deuda.deudorId(),
                deuda.acreedorId(),
                deuda.monto().monto(),
                deuda.monto().moneda(),
                mapEstado(deuda.estado()),
                deuda.fechaPago()
        );
    }

    private static String mapEstado(EstadoDeuda estado) {
        return switch (estado) {
            case PENDIENTE -> "PENDING";
            case PAGADA -> "PAID";
        };
    }
}
