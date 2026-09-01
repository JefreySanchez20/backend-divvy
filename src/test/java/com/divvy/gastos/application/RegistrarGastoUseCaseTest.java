package com.divvy.gastos.application;

import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.GastoRepository;
import com.divvy.gastos.domain.TipoDivision;
import com.divvy.gastos.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.DomainEvent;
import com.divvy.shared.domain.DomainEventPublisher;
import com.divvy.shared.domain.events.GastoRegistradoEvent;
import com.divvy.shared.domain.exception.InvariantViolationException;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarGastoUseCaseTest {

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private VerificadorMiembroGrupo verificadorMiembroGrupo;

    @Mock
    private DomainEventPublisher eventPublisher;

    @Test
    void ejecutar_datosValidos_guardaYPublicaEvento() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(true);
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, pagadoPor)).thenReturn(true);
        when(gastoRepository.guardar(any(Gasto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrarGastoUseCase useCase = new RegistrarGastoUseCase(gastoRepository, verificadorMiembroGrupo, eventPublisher);
        Gasto resultado = useCase.ejecutar(
                actorId, grupoId, "Cena", new BigDecimal("50.00"), "PEN", pagadoPor,
                Instant.now(), "Comida", TipoDivision.IGUAL, Map.of(pagadoPor, BigDecimal.ZERO));

        assertThat(resultado.descripcion()).isEqualTo("Cena");

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(eventPublisher).publicar(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(GastoRegistradoEvent.class);
        GastoRegistradoEvent evento = (GastoRegistradoEvent) captor.getValue();
        assertThat(evento.grupoId()).isEqualTo(grupoId);
        assertThat(evento.monto()).isEqualByComparingTo("50.00");
    }

    @Test
    void ejecutar_actorNoEsMiembro_lanzaUnauthorizedYNoGuardaNiPublica() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(false);

        RegistrarGastoUseCase useCase = new RegistrarGastoUseCase(gastoRepository, verificadorMiembroGrupo, eventPublisher);

        assertThatThrownBy(() -> useCase.ejecutar(
                actorId, grupoId, "Cena", new BigDecimal("50.00"), "PEN", pagadoPor,
                Instant.now(), "Comida", TipoDivision.IGUAL, Map.of(pagadoPor, BigDecimal.ZERO)))
                .isInstanceOf(UnauthorizedOperationException.class);

        verify(gastoRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicar(any());
    }

    @Test
    void ejecutar_pagadoPorNoEsMiembro_lanzaInvariantViolation() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(true);
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, pagadoPor)).thenReturn(false);

        RegistrarGastoUseCase useCase = new RegistrarGastoUseCase(gastoRepository, verificadorMiembroGrupo, eventPublisher);

        assertThatThrownBy(() -> useCase.ejecutar(
                actorId, grupoId, "Cena", new BigDecimal("50.00"), "PEN", pagadoPor,
                Instant.now(), "Comida", TipoDivision.IGUAL, Map.of(pagadoPor, BigDecimal.ZERO)))
                .isInstanceOf(InvariantViolationException.class);
    }
}
