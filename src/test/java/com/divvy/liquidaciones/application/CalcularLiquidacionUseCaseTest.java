package com.divvy.liquidaciones.application;

import com.divvy.liquidaciones.domain.CalculadoraDeudas;
import com.divvy.liquidaciones.domain.LectorBalanceGrupo;
import com.divvy.liquidaciones.domain.Liquidacion;
import com.divvy.liquidaciones.domain.LiquidacionRepository;
import com.divvy.shared.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularLiquidacionUseCaseTest {

    @Mock
    private LectorBalanceGrupo lectorBalanceGrupo;

    @Mock
    private LiquidacionRepository liquidacionRepository;

    @Mock
    private VerificadorMiembroGrupo verificadorMiembroGrupo;

    private final CalculadoraDeudas calculadoraDeudas = new CalculadoraDeudas();

    @Test
    void ejecutar_actorEsMiembro_calculaYGuardaLaLiquidacion() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID deudor = UUID.randomUUID();
        UUID acreedor = UUID.randomUUID();

        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(true);
        when(lectorBalanceGrupo.obtenerBalances(grupoId)).thenReturn(Map.of(
                "PEN", Map.of(deudor, new BigDecimal("-50.00"), acreedor, new BigDecimal("50.00"))
        ));
        when(liquidacionRepository.guardar(any(Liquidacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CalcularLiquidacionUseCase useCase = new CalcularLiquidacionUseCase(
                lectorBalanceGrupo, calculadoraDeudas, liquidacionRepository, verificadorMiembroGrupo);

        Liquidacion resultado = useCase.ejecutar(actorId, grupoId);

        assertThat(resultado.grupoId()).isEqualTo(grupoId);
        assertThat(resultado.deudas()).hasSize(1);
        assertThat(resultado.deudas().get(0).deudorId()).isEqualTo(deudor);
        assertThat(resultado.deudas().get(0).acreedorId()).isEqualTo(acreedor);
        assertThat(resultado.deudas().get(0).monto().monto()).isEqualByComparingTo("50.00");
    }

    @Test
    void ejecutar_sinDeudasPendientes_generaLiquidacionVacia() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(true);
        when(lectorBalanceGrupo.obtenerBalances(grupoId)).thenReturn(Map.of());
        when(liquidacionRepository.guardar(any(Liquidacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CalcularLiquidacionUseCase useCase = new CalcularLiquidacionUseCase(
                lectorBalanceGrupo, calculadoraDeudas, liquidacionRepository, verificadorMiembroGrupo);

        Liquidacion resultado = useCase.ejecutar(actorId, grupoId);

        assertThat(resultado.deudas()).isEmpty();
    }

    @Test
    void ejecutar_actorNoEsMiembro_lanzaUnauthorized() {
        UUID grupoId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        when(verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)).thenReturn(false);

        CalcularLiquidacionUseCase useCase = new CalcularLiquidacionUseCase(
                lectorBalanceGrupo, calculadoraDeudas, liquidacionRepository, verificadorMiembroGrupo);

        assertThatThrownBy(() -> useCase.ejecutar(actorId, grupoId))
                .isInstanceOf(UnauthorizedOperationException.class);
    }
}
