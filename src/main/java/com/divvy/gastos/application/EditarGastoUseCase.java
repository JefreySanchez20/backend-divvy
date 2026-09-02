package com.divvy.gastos.application;

import com.divvy.gastos.domain.DivisionGasto;
import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.GastoRepository;
import com.divvy.gastos.domain.TipoDivision;
import com.divvy.shared.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.InvariantViolationException;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class EditarGastoUseCase {

    private final GastoRepository gastoRepository;
    private final VerificadorMiembroGrupo verificadorMiembroGrupo;

    public EditarGastoUseCase(GastoRepository gastoRepository, VerificadorMiembroGrupo verificadorMiembroGrupo) {
        this.gastoRepository = gastoRepository;
        this.verificadorMiembroGrupo = verificadorMiembroGrupo;
    }

    public Gasto ejecutar(
            UUID actorId, UUID grupoId, UUID gastoId, String descripcion, BigDecimal monto, String moneda,
            UUID pagadoPor, Instant fecha, String categoria, TipoDivision tipoDivision, Map<UUID, BigDecimal> detalleDivision
    ) {
        Gasto gasto = gastoRepository.buscarPorId(gastoId)
                .filter(g -> g.grupoId().equals(grupoId))
                .orElseThrow(() -> new EntityNotFoundException("Gasto no encontrado: " + gastoId));

        if (!verificadorMiembroGrupo.esMiembroActivo(gasto.grupoId(), actorId)) {
            throw new UnauthorizedOperationException("Debes ser miembro del grupo para editar este gasto");
        }
        if (!verificadorMiembroGrupo.esMiembroActivo(gasto.grupoId(), pagadoPor)) {
            throw new InvariantViolationException("El usuario que pagó debe ser miembro activo del grupo");
        }

        Dinero dinero = Dinero.de(monto, moneda);
        DivisionGasto division = DivisionGasto.crear(tipoDivision, dinero, detalleDivision);

        gasto.editar(descripcion, dinero, pagadoPor, fecha, categoria, division);
        return gastoRepository.guardar(gasto);
    }
}
