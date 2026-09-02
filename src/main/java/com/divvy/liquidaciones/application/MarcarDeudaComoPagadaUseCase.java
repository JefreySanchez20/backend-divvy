package com.divvy.liquidaciones.application;

import com.divvy.liquidaciones.domain.Deuda;
import com.divvy.liquidaciones.domain.Liquidacion;
import com.divvy.liquidaciones.domain.LiquidacionRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

import java.util.UUID;

public class MarcarDeudaComoPagadaUseCase {

    private final LiquidacionRepository liquidacionRepository;

    public MarcarDeudaComoPagadaUseCase(LiquidacionRepository liquidacionRepository) {
        this.liquidacionRepository = liquidacionRepository;
    }

    public Liquidacion ejecutar(UUID actorId, UUID liquidacionId, UUID deudaId) {
        Liquidacion liquidacion = liquidacionRepository.buscarPorId(liquidacionId)
                .orElseThrow(() -> new EntityNotFoundException("Liquidación no encontrada: " + liquidacionId));

        Deuda deuda = liquidacion.buscarDeuda(deudaId)
                .orElseThrow(() -> new EntityNotFoundException("Deuda no encontrada: " + deudaId));

        if (!actorId.equals(deuda.deudorId()) && !actorId.equals(deuda.acreedorId())) {
            throw new UnauthorizedOperationException("Solo el deudor o el acreedor pueden marcar esta deuda como pagada");
        }

        deuda.marcarComoPagada();
        return liquidacionRepository.guardar(liquidacion);
    }
}
