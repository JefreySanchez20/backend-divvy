package com.divvy.grupos;

import com.divvy.grupos.application.AgregarMiembroUseCase;
import com.divvy.grupos.application.ArchivarGrupoUseCase;
import com.divvy.grupos.application.CrearGrupoUseCase;
import com.divvy.grupos.application.ListarGruposDelUsuarioUseCase;
import com.divvy.grupos.application.ObtenerGrupoUseCase;
import com.divvy.grupos.application.RemoverMiembroUseCase;
import com.divvy.grupos.domain.GrupoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrupoConfig {

    @Bean
    public CrearGrupoUseCase crearGrupoUseCase(GrupoRepository grupoRepository) {
        return new CrearGrupoUseCase(grupoRepository);
    }

    @Bean
    public ListarGruposDelUsuarioUseCase listarGruposDelUsuarioUseCase(GrupoRepository grupoRepository) {
        return new ListarGruposDelUsuarioUseCase(grupoRepository);
    }

    @Bean
    public ObtenerGrupoUseCase obtenerGrupoUseCase(GrupoRepository grupoRepository) {
        return new ObtenerGrupoUseCase(grupoRepository);
    }

    @Bean
    public AgregarMiembroUseCase agregarMiembroUseCase(GrupoRepository grupoRepository) {
        return new AgregarMiembroUseCase(grupoRepository);
    }

    @Bean
    public RemoverMiembroUseCase removerMiembroUseCase(GrupoRepository grupoRepository) {
        return new RemoverMiembroUseCase(grupoRepository);
    }

    @Bean
    public ArchivarGrupoUseCase archivarGrupoUseCase(GrupoRepository grupoRepository) {
        return new ArchivarGrupoUseCase(grupoRepository);
    }
}
