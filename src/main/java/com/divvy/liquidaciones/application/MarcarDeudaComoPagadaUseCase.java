package com.divvy.liquidaciones.application;

import com.divvy.liquidaciones.domain.Deuda;
import com.divvy.liquidaciones.domain.EstadoDeuda;
import com.divvy.liquidaciones.domain.Liquidacion;
import com.divvy.liquidaciones.domain.LiquidacionRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.InvariantViolationException;
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

        // Si ya estaba pagada, marcarComoPagada() da el mensaje correcto ("ya fue pagada")
        // aunque la liquidación sea vieja, así que la vigencia solo se exige a las pendientes.
        if (deuda.estado() == EstadoDeuda.PENDIENTE) {
            exigirLiquidacionVigente(liquidacion);
        }

        deuda.marcarComoPagada();
        return liquidacionRepository.guardar(liquidacion);
    }

    /**
     * Cada consulta de liquidación crea una nueva con deudas de ids nuevos, y los pagos
     * descuentan del balance. Si se pudieran pagar deudas de una liquidación vieja, la misma
     * deuda pagada desde dos liquidaciones distintas se descontaría dos veces.
     */
    private void exigirLiquidacionVigente(Liquidacion liquidacion) {
        boolean esLaUltima = liquidacionRepository.buscarUltimaPorGrupo(liquidacion.grupoId())
                .map(ultima -> ultima.id().equals(liquidacion.id()))
                .orElse(false);
        if (!esLaUltima) {
            throw new InvariantViolationException(
                    "Esta liquidación quedó desactualizada porque se calculó una más reciente. "
                            + "Vuelve a cargar las deudas del grupo e inténtalo de nuevo");
        }
    }
}
