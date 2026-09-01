package com.divvy.gastos.application;

import com.divvy.gastos.domain.DivisionGasto;
import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.GastoRepository;
import com.divvy.gastos.domain.TipoDivision;
import com.divvy.gastos.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EliminarGastoUseCaseTest {

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private VerificadorMiembroGrupo verificadorMiembroGrupo;

    @Test
    void ejecutar_actorEsMiembro_eliminaElGasto() {
        UUID grupoId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto division = DivisionGasto.crear(TipoDivision.IGUAL, monto, Map.of(pagadoPor, BigDecimal.ZERO));
        Gasto gasto = Gasto.registrar(UUID.randomUUID(), grupoId, "Cena", monto, pagadoPor, Instant.now(), "Comida", division);

        when(gastoRepository.buscarPorId(gasto.id())).thenReturn(Optional.of(gasto));
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(true);

        EliminarGastoUseCase useCase = new EliminarGastoUseCase(gastoRepository, verificadorMiembroGrupo);
        useCase.ejecutar(actorId, grupoId, gasto.id());

        verify(gastoRepository).eliminar(gasto.id());
    }

    @Test
    void ejecutar_gastoNoExiste_lanzaEntityNotFound() {
        UUID gastoId = UUID.randomUUID();
        when(gastoRepository.buscarPorId(gastoId)).thenReturn(Optional.empty());

        EliminarGastoUseCase useCase = new EliminarGastoUseCase(gastoRepository, verificadorMiembroGrupo);

        assertThatThrownBy(() -> useCase.ejecutar(UUID.randomUUID(), UUID.randomUUID(), gastoId))
                .isInstanceOf(EntityNotFoundException.class);
        verify(gastoRepository, never()).eliminar(any());
    }

    @Test
    void ejecutar_actorNoEsMiembro_lanzaUnauthorized() {
        UUID grupoId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto division = DivisionGasto.crear(TipoDivision.IGUAL, monto, Map.of(pagadoPor, BigDecimal.ZERO));
        Gasto gasto = Gasto.registrar(UUID.randomUUID(), grupoId, "Cena", monto, pagadoPor, Instant.now(), "Comida", division);

        when(gastoRepository.buscarPorId(gasto.id())).thenReturn(Optional.of(gasto));
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(false);

        EliminarGastoUseCase useCase = new EliminarGastoUseCase(gastoRepository, verificadorMiembroGrupo);

        assertThatThrownBy(() -> useCase.ejecutar(actorId, grupoId, gasto.id()))
                .isInstanceOf(UnauthorizedOperationException.class);
        verify(gastoRepository, never()).eliminar(any());
    }

    @Test
    void ejecutar_gastoPerteneceAOtroGrupo_lanzaEntityNotFound() {
        UUID grupoId = UUID.randomUUID();
        UUID otroGrupoId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto division = DivisionGasto.crear(TipoDivision.IGUAL, monto, Map.of(pagadoPor, BigDecimal.ZERO));
        Gasto gasto = Gasto.registrar(UUID.randomUUID(), grupoId, "Cena", monto, pagadoPor, Instant.now(), "Comida", division);

        when(gastoRepository.buscarPorId(gasto.id())).thenReturn(Optional.of(gasto));

        EliminarGastoUseCase useCase = new EliminarGastoUseCase(gastoRepository, verificadorMiembroGrupo);

        assertThatThrownBy(() -> useCase.ejecutar(UUID.randomUUID(), otroGrupoId, gasto.id()))
                .isInstanceOf(EntityNotFoundException.class);
        verify(gastoRepository, never()).eliminar(any());
    }
}
