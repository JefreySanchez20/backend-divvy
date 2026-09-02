package com.divvy.liquidaciones;

import com.divvy.liquidaciones.application.CalcularLiquidacionUseCase;
import com.divvy.liquidaciones.application.MarcarDeudaComoPagadaUseCase;
import com.divvy.liquidaciones.application.ObtenerHistorialLiquidacionesUseCase;
import com.divvy.liquidaciones.domain.CalculadoraDeudas;
import com.divvy.liquidaciones.domain.LectorBalanceGrupo;
import com.divvy.liquidaciones.domain.LiquidacionRepository;
import com.divvy.shared.domain.VerificadorMiembroGrupo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquidacionConfig {

    @Bean
    public CalculadoraDeudas calculadoraDeudas() {
        return new CalculadoraDeudas();
    }

    @Bean
    public CalcularLiquidacionUseCase calcularLiquidacionUseCase(
            LectorBalanceGrupo lectorBalanceGrupo,
            CalculadoraDeudas calculadoraDeudas,
            LiquidacionRepository liquidacionRepository,
            VerificadorMiembroGrupo verificadorMiembroGrupo
    ) {
        return new CalcularLiquidacionUseCase(lectorBalanceGrupo, calculadoraDeudas, liquidacionRepository, verificadorMiembroGrupo);
    }

    @Bean
    public ObtenerHistorialLiquidacionesUseCase obtenerHistorialLiquidacionesUseCase(
            LiquidacionRepository liquidacionRepository, VerificadorMiembroGrupo verificadorMiembroGrupo
    ) {
        return new ObtenerHistorialLiquidacionesUseCase(liquidacionRepository, verificadorMiembroGrupo);
    }

    @Bean
    public MarcarDeudaComoPagadaUseCase marcarDeudaComoPagadaUseCase(LiquidacionRepository liquidacionRepository) {
        return new MarcarDeudaComoPagadaUseCase(liquidacionRepository);
    }
}
