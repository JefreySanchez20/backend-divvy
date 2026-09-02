package com.divvy.gastos.application;

import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.GastoRepository;
import com.divvy.shared.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

import java.util.UUID;

public class EliminarGastoUseCase {

    private final GastoRepository gastoRepository;
    private final VerificadorMiembroGrupo verificadorMiembroGrupo;

    public EliminarGastoUseCase(GastoRepository gastoRepository, VerificadorMiembroGrupo verificadorMiembroGrupo) {
        this.gastoRepository = gastoRepository;
        this.verificadorMiembroGrupo = verificadorMiembroGrupo;
    }

    public void ejecutar(UUID actorId, UUID grupoId, UUID gastoId) {
        Gasto gasto = gastoRepository.buscarPorId(gastoId)
                .filter(g -> g.grupoId().equals(grupoId))
                .orElseThrow(() -> new EntityNotFoundException("Gasto no encontrado: " + gastoId));

        if (!verificadorMiembroGrupo.esMiembroActivo(gasto.grupoId(), actorId)) {
            throw new UnauthorizedOperationException("Debes ser miembro del grupo para eliminar este gasto");
        }

        gastoRepository.eliminar(gastoId);
    }
}
