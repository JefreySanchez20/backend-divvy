package com.divvy.liquidaciones.application;

import com.divvy.liquidaciones.domain.Liquidacion;
import com.divvy.liquidaciones.domain.LiquidacionRepository;
import com.divvy.shared.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObtenerHistorialLiquidacionesUseCaseTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @Mock
    private VerificadorMiembroGrupo verificadorMiembroGrupo;

    @Test
    void ejecutar_actorEsMiembro_devuelveElHistorial() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Liquidacion liquidacion = Liquidacion.calcular(UUID.randomUUID(), grupoId, List.of());

        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(true);
        when(liquidacionRepository.buscarPorGrupo(grupoId)).thenReturn(List.of(liquidacion));

        ObtenerHistorialLiquidacionesUseCase useCase = new ObtenerHistorialLiquidacionesUseCase(
                liquidacionRepository, verificadorMiembroGrupo);

        assertThat(useCase.ejecutar(actorId, grupoId)).containsExactly(liquidacion);
    }

    @Test
    void ejecutar_actorNoEsMiembro_lanzaUnauthorized() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(false);

        ObtenerHistorialLiquidacionesUseCase useCase = new ObtenerHistorialLiquidacionesUseCase(
                liquidacionRepository, verificadorMiembroGrupo);

        assertThatThrownBy(() -> useCase.ejecutar(actorId, grupoId))
                .isInstanceOf(UnauthorizedOperationException.class);
    }
}
