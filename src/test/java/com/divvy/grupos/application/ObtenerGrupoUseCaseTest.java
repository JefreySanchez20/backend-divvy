package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

@ExtendWith(MockitoExtension.class)
class ObtenerGrupoUseCaseTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Test
    void ejecutar_grupoExiste_loRetorna() {
        UUID grupoId = UUID.randomUUID();
        UUID miembroId = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", "PEN", miembroId);
        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));

        ObtenerGrupoUseCase useCase = new ObtenerGrupoUseCase(grupoRepository);

        assertThat(useCase.ejecutar(grupoId, miembroId)).isSameAs(grupo);
    }

    @Test
    void ejecutar_grupoNoExiste_lanzaEntityNotFound() {
        UUID grupoId = UUID.randomUUID();
        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.empty());

        ObtenerGrupoUseCase useCase = new ObtenerGrupoUseCase(grupoRepository);

        assertThatThrownBy(() -> useCase.ejecutar(grupoId, UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void ejecutar_actorNoEsMiembro_lanzaUnauthorized() {
        UUID grupoId = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", "PEN", UUID.randomUUID());
        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));

        ObtenerGrupoUseCase useCase = new ObtenerGrupoUseCase(grupoRepository);

        assertThatThrownBy(() -> useCase.ejecutar(grupoId, UUID.randomUUID()))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    void ejecutar_grupoArchivado_sigueVisibleParaSusMiembros() {
        UUID grupoId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", "PEN", adminId);
        grupo.archivar(adminId);
        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));

        ObtenerGrupoUseCase useCase = new ObtenerGrupoUseCase(grupoRepository);

        assertThat(useCase.ejecutar(grupoId, adminId)).isSameAs(grupo);
    }
}
