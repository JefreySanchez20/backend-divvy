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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditarGastoUseCaseTest {

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private VerificadorMiembroGrupo verificadorMiembroGrupo;

    private Gasto gastoExistente(UUID grupoId, UUID pagadoPor) {
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto division = DivisionGasto.crear(TipoDivision.IGUAL, monto, Map.of(pagadoPor, BigDecimal.ZERO));
        return Gasto.registrar(UUID.randomUUID(), grupoId, "Cena", monto, pagadoPor, Instant.now(), "Comida", division);
    }

    @Test
    void ejecutar_gastoExiste_editaYGuarda() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        Gasto gasto = gastoExistente(grupoId, pagadoPor);

        when(gastoRepository.buscarPorId(gasto.id())).thenReturn(Optional.of(gasto));
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(true);
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, pagadoPor)).thenReturn(true);
        when(gastoRepository.guardar(any(Gasto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EditarGastoUseCase useCase = new EditarGastoUseCase(gastoRepository, verificadorMiembroGrupo);
        Gasto resultado = useCase.ejecutar(
                actorId, grupoId, gasto.id(), "Cena actualizada", new BigDecimal("70.00"), "PEN", pagadoPor,
                Instant.now(), "Restaurante", TipoDivision.IGUAL, Map.of(pagadoPor, BigDecimal.ZERO));

        assertThat(resultado.descripcion()).isEqualTo("Cena actualizada");
        assertThat(resultado.monto().monto()).isEqualByComparingTo("70.00");
    }

    @Test
    void ejecutar_gastoNoExiste_lanzaEntityNotFound() {
        UUID gastoId = UUID.randomUUID();
        when(gastoRepository.buscarPorId(gastoId)).thenReturn(Optional.empty());

        EditarGastoUseCase useCase = new EditarGastoUseCase(gastoRepository, verificadorMiembroGrupo);

        assertThatThrownBy(() -> useCase.ejecutar(
                UUID.randomUUID(), UUID.randomUUID(), gastoId, "Cena", new BigDecimal("50.00"), "PEN", UUID.randomUUID(),
                Instant.now(), "Comida", TipoDivision.IGUAL, Map.of(UUID.randomUUID(), BigDecimal.ZERO)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void ejecutar_actorNoEsMiembro_lanzaUnauthorized() {
        UUID grupoId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Gasto gasto = gastoExistente(grupoId, pagadoPor);

        when(gastoRepository.buscarPorId(gasto.id())).thenReturn(Optional.of(gasto));
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(false);

        EditarGastoUseCase useCase = new EditarGastoUseCase(gastoRepository, verificadorMiembroGrupo);

        assertThatThrownBy(() -> useCase.ejecutar(
                actorId, grupoId, gasto.id(), "Cena", new BigDecimal("50.00"), "PEN", pagadoPor,
                Instant.now(), "Comida", TipoDivision.IGUAL, Map.of(pagadoPor, BigDecimal.ZERO)))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void ejecutar_gastoPerteneceAOtroGrupo_lanzaEntityNotFound() {
        UUID grupoId = UUID.randomUUID();
        UUID otroGrupoId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        Gasto gasto = gastoExistente(grupoId, pagadoPor);

        when(gastoRepository.buscarPorId(gasto.id())).thenReturn(Optional.of(gasto));

        EditarGastoUseCase useCase = new EditarGastoUseCase(gastoRepository, verificadorMiembroGrupo);

        assertThatThrownBy(() -> useCase.ejecutar(
                UUID.randomUUID(), otroGrupoId, gasto.id(), "Cena", new BigDecimal("50.00"), "PEN", pagadoPor,
                Instant.now(), "Comida", TipoDivision.IGUAL, Map.of(pagadoPor, BigDecimal.ZERO)))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
