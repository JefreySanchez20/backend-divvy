package com.divvy.gastos.application;

import com.divvy.gastos.domain.DivisionGasto;
import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.GastoRepository;
import com.divvy.gastos.domain.TipoDivision;
import com.divvy.shared.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.DomainEventPublisher;
import com.divvy.shared.domain.events.GastoRegistradoEvent;
import com.divvy.shared.domain.exception.InvariantViolationException;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class RegistrarGastoUseCase {

    private final GastoRepository gastoRepository;
    private final VerificadorMiembroGrupo verificadorMiembroGrupo;
    private final DomainEventPublisher eventPublisher;

    public RegistrarGastoUseCase(
            GastoRepository gastoRepository,
            VerificadorMiembroGrupo verificadorMiembroGrupo,
            DomainEventPublisher eventPublisher
    ) {
        this.gastoRepository = gastoRepository;
        this.verificadorMiembroGrupo = verificadorMiembroGrupo;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Gasto ejecutar(
            UUID actorId, UUID grupoId, String descripcion, BigDecimal monto, String moneda,
            UUID pagadoPor, Instant fecha, String categoria, TipoDivision tipoDivision, Map<UUID, BigDecimal> detalleDivision
    ) {
        if (!verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)) {
            throw new UnauthorizedOperationException("Debes ser miembro del grupo para registrar gastos");
        }
        if (!verificadorMiembroGrupo.esMiembroActivo(grupoId, pagadoPor)) {
            throw new InvariantViolationException("El usuario que pagó debe ser miembro activo del grupo");
        }

        Dinero dinero = Dinero.de(monto, moneda);
        DivisionGasto division = DivisionGasto.crear(tipoDivision, dinero, detalleDivision);

        Gasto gasto = Gasto.registrar(UUID.randomUUID(), grupoId, descripcion, dinero, pagadoPor, fecha, categoria, division);
        Gasto guardado = gastoRepository.guardar(gasto);

        eventPublisher.publicar(new GastoRegistradoEvent(
                grupoId, guardado.id(), pagadoPor, dinero.monto(), dinero.moneda(), division.detalle()));

        return guardado;
    }
}
