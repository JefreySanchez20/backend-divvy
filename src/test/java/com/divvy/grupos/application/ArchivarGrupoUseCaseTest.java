package com.divvy.grupos.application;

import com.divvy.grupos.domain.EstadoGrupo;
import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArchivarGrupoUseCaseTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Test
    void ejecutar_actorEsAdmin_archivaYGuarda() {
        UUID grupoId = UUID.randomUUID();
        UUID creadorId = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", creadorId);
        grupo.agregarMiembro(UUID.randomUUID());

        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));
        when(grupoRepository.guardar(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArchivarGrupoUseCase useCase = new ArchivarGrupoUseCase(grupoRepository);
        Grupo resultado = useCase.ejecutar(grupoId, creadorId);

        assertThat(resultado.estado()).isEqualTo(EstadoGrupo.ARCHIVADO);
    }

    @Test
    void ejecutar_actorNoEsAdmin_lanzaUnauthorized() {
        UUID grupoId = UUID.randomUUID();
        UUID creadorId = UUID.randomUUID();
        UUID miembro2 = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", creadorId);
        grupo.agregarMiembro(miembro2);

        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));

        ArchivarGrupoUseCase useCase = new ArchivarGrupoUseCase(grupoRepository);

        assertThatThrownBy(() -> useCase.ejecutar(grupoId, miembro2))
                .isInstanceOf(UnauthorizedOperationException.class);
    }
}
