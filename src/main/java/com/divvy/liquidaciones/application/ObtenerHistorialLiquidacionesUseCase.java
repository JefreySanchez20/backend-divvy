package com.divvy.liquidaciones.application;

import com.divvy.liquidaciones.domain.Liquidacion;
import com.divvy.liquidaciones.domain.LiquidacionRepository;
import com.divvy.shared.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

import java.util.List;
import java.util.UUID;

public class ObtenerHistorialLiquidacionesUseCase {

    private final LiquidacionRepository liquidacionRepository;
    private final VerificadorMiembroGrupo verificadorMiembroGrupo;

    public ObtenerHistorialLiquidacionesUseCase(
            LiquidacionRepository liquidacionRepository, VerificadorMiembroGrupo verificadorMiembroGrupo
    ) {
        this.liquidacionRepository = liquidacionRepository;
        this.verificadorMiembroGrupo = verificadorMiembroGrupo;
    }

    public List<Liquidacion> ejecutar(UUID actorId, UUID grupoId) {
        if (!verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)) {
            throw new UnauthorizedOperationException("Debes ser miembro del grupo para ver su historial de liquidaciones");
        }
        return liquidacionRepository.buscarPorGrupo(grupoId);
    }
}
