package com.divvy.gastos;

import com.divvy.gastos.application.EditarGastoUseCase;
import com.divvy.gastos.application.EliminarGastoUseCase;
import com.divvy.gastos.application.ListarGastosPorGrupoUseCase;
import com.divvy.gastos.application.ObtenerGastoUseCase;
import com.divvy.gastos.application.RegistrarGastoUseCase;
import com.divvy.gastos.domain.GastoRepository;
import com.divvy.gastos.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.DomainEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GastoConfig {

    @Bean
    public RegistrarGastoUseCase registrarGastoUseCase(
            GastoRepository gastoRepository, VerificadorMiembroGrupo verificadorMiembroGrupo, DomainEventPublisher eventPublisher
    ) {
        return new RegistrarGastoUseCase(gastoRepository, verificadorMiembroGrupo, eventPublisher);
    }

    @Bean
    public ListarGastosPorGrupoUseCase listarGastosPorGrupoUseCase(
            GastoRepository gastoRepository, VerificadorMiembroGrupo verificadorMiembroGrupo
    ) {
        return new ListarGastosPorGrupoUseCase(gastoRepository, verificadorMiembroGrupo);
    }

    @Bean
    public ObtenerGastoUseCase obtenerGastoUseCase(
            GastoRepository gastoRepository, VerificadorMiembroGrupo verificadorMiembroGrupo
    ) {
        return new ObtenerGastoUseCase(gastoRepository, verificadorMiembroGrupo);
    }

    @Bean
    public EditarGastoUseCase editarGastoUseCase(
            GastoRepository gastoRepository, VerificadorMiembroGrupo verificadorMiembroGrupo
    ) {
        return new EditarGastoUseCase(gastoRepository, verificadorMiembroGrupo);
    }

    @Bean
    public EliminarGastoUseCase eliminarGastoUseCase(
            GastoRepository gastoRepository, VerificadorMiembroGrupo verificadorMiembroGrupo
    ) {
        return new EliminarGastoUseCase(gastoRepository, verificadorMiembroGrupo);
    }
}
