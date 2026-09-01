package com.divvy.gastos.application;

import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.GastoRepository;
import com.divvy.gastos.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

import java.util.List;
import java.util.UUID;

public class ListarGastosPorGrupoUseCase {

    private final GastoRepository gastoRepository;
    private final VerificadorMiembroGrupo verificadorMiembroGrupo;

    public ListarGastosPorGrupoUseCase(GastoRepository gastoRepository, VerificadorMiembroGrupo verificadorMiembroGrupo) {
        this.gastoRepository = gastoRepository;
        this.verificadorMiembroGrupo = verificadorMiembroGrupo;
    }

    public List<Gasto> ejecutar(UUID actorId, UUID grupoId) {
        if (!verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)) {
            throw new UnauthorizedOperationException("Debes ser miembro del grupo para ver sus gastos");
        }
        return gastoRepository.buscarPorGrupo(grupoId);
    }
}
