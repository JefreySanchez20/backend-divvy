package com.divvy.liquidaciones.application;

import com.divvy.liquidaciones.domain.Deuda;
import com.divvy.liquidaciones.domain.EstadoDeuda;
import com.divvy.liquidaciones.domain.Liquidacion;
import com.divvy.liquidaciones.domain.LiquidacionRepository;
import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarcarDeudaComoPagadaUseCaseTest {

    @Mock
    private LiquidacionRepository liquidacionRepository;

    private Liquidacion liquidacionConUnaDeuda(UUID deudorId, UUID acreedorId) {
        Dinero monto = Dinero.de(new BigDecimal("30.00"), "PEN");
        Deuda deuda = Deuda.crear(UUID.randomUUID(), deudorId, acreedorId, monto);
        return Liquidacion.calcular(UUID.randomUUID(), UUID.randomUUID(), List.of(deuda));
    }

    @Test
    void ejecutar_actorEsElDeudor_marcaComoPagada() {
        UUID deudorId = UUID.randomUUID();
        UUID acreedorId = UUID.randomUUID();
        Liquidacion liquidacion = liquidacionConUnaDeuda(deudorId, acreedorId);
        UUID deudaId = liquidacion.deudas().get(0).id();

        when(liquidacionRepository.buscarPorId(liquidacion.id())).thenReturn(Optional.of(liquidacion));
        when(liquidacionRepository.guardar(any(Liquidacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarcarDeudaComoPagadaUseCase useCase = new MarcarDeudaComoPagadaUseCase(liquidacionRepository);
        Liquidacion resultado = useCase.ejecutar(deudorId, liquidacion.id(), deudaId);

        assertThat(resultado.buscarDeuda(deudaId).orElseThrow().estado()).isEqualTo(EstadoDeuda.PAGADA);
    }

    @Test
    void ejecutar_actorEsElAcreedor_marcaComoPagada() {
        UUID deudorId = UUID.randomUUID();
        UUID acreedorId = UUID.randomUUID();
        Liquidacion liquidacion = liquidacionConUnaDeuda(deudorId, acreedorId);
        UUID deudaId = liquidacion.deudas().get(0).id();

        when(liquidacionRepository.buscarPorId(liquidacion.id())).thenReturn(Optional.of(liquidacion));
        when(liquidacionRepository.guardar(any(Liquidacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MarcarDeudaComoPagadaUseCase useCase = new MarcarDeudaComoPagadaUseCase(liquidacionRepository);
        Liquidacion resultado = useCase.ejecutar(acreedorId, liquidacion.id(), deudaId);

        assertThat(resultado.buscarDeuda(deudaId).orElseThrow().estado()).isEqualTo(EstadoDeuda.PAGADA);
    }

    @Test
    void ejecutar_actorAjenoALaDeuda_lanzaUnauthorized() {
        UUID deudorId = UUID.randomUUID();
        UUID acreedorId = UUID.randomUUID();
        UUID ajeno = UUID.randomUUID();
        Liquidacion liquidacion = liquidacionConUnaDeuda(deudorId, acreedorId);
        UUID deudaId = liquidacion.deudas().get(0).id();

        when(liquidacionRepository.buscarPorId(liquidacion.id())).thenReturn(Optional.of(liquidacion));

        MarcarDeudaComoPagadaUseCase useCase = new MarcarDeudaComoPagadaUseCase(liquidacionRepository);

        assertThatThrownBy(() -> useCase.ejecutar(ajeno, liquidacion.id(), deudaId))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void ejecutar_liquidacionNoExiste_lanzaEntityNotFound() {
        UUID liquidacionId = UUID.randomUUID();
        when(liquidacionRepository.buscarPorId(liquidacionId)).thenReturn(Optional.empty());

        MarcarDeudaComoPagadaUseCase useCase = new MarcarDeudaComoPagadaUseCase(liquidacionRepository);

        assertThatThrownBy(() -> useCase.ejecutar(UUID.randomUUID(), liquidacionId, UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void ejecutar_deudaNoExisteEnLaLiquidacion_lanzaEntityNotFound() {
        Liquidacion liquidacion = liquidacionConUnaDeuda(UUID.randomUUID(), UUID.randomUUID());
        when(liquidacionRepository.buscarPorId(liquidacion.id())).thenReturn(Optional.of(liquidacion));

        MarcarDeudaComoPagadaUseCase useCase = new MarcarDeudaComoPagadaUseCase(liquidacionRepository);

        assertThatThrownBy(() -> useCase.ejecutar(UUID.randomUUID(), liquidacion.id(), UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
