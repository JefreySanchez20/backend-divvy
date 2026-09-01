package com.divvy.gastos.application;

import com.divvy.gastos.domain.DivisionGasto;
import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.GastoRepository;
import com.divvy.gastos.domain.TipoDivision;
import com.divvy.gastos.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarGastosPorGrupoUseCaseTest {

    @Mock
    private GastoRepository gastoRepository;

    @Mock
    private VerificadorMiembroGrupo verificadorMiembroGrupo;

    @Test
    void ejecutar_actorEsMiembro_devuelveLosGastos() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID pagadoPor = UUID.randomUUID();
        Dinero monto = Dinero.de(new BigDecimal("50.00"), "PEN");
        DivisionGasto division = DivisionGasto.crear(TipoDivision.IGUAL, monto, Map.of(pagadoPor, BigDecimal.ZERO));
        Gasto gasto = Gasto.registrar(UUID.randomUUID(), grupoId, "Cena", monto, pagadoPor, Instant.now(), "Comida", division);

        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(true);
        when(gastoRepository.buscarPorGrupo(grupoId)).thenReturn(List.of(gasto));

        ListarGastosPorGrupoUseCase useCase = new ListarGastosPorGrupoUseCase(gastoRepository, verificadorMiembroGrupo);

        assertThat(useCase.ejecutar(actorId, grupoId)).containsExactly(gasto);
    }

    @Test
    void ejecutar_actorNoEsMiembro_lanzaUnauthorized() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(false);

        ListarGastosPorGrupoUseCase useCase = new ListarGastosPorGrupoUseCase(gastoRepository, verificadorMiembroGrupo);

        assertThatThrownBy(() -> useCase.ejecutar(actorId, grupoId))
                .isInstanceOf(UnauthorizedOperationException.class);
    }
}
