package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;
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
class RemoverMiembroUseCaseTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Test
    void ejecutar_actorEsAdminYQuedanSuficientesMiembros_remueveYGuarda() {
        UUID grupoId = UUID.randomUUID();
        UUID creadorId = UUID.randomUUID();
        UUID miembro2 = UUID.randomUUID();
        UUID miembro3 = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", creadorId);
        grupo.agregarMiembro(miembro2);
        grupo.agregarMiembro(miembro3);

        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));
        when(grupoRepository.guardar(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RemoverMiembroUseCase useCase = new RemoverMiembroUseCase(grupoRepository);
        Grupo resultado = useCase.ejecutar(grupoId, creadorId, miembro3);

        assertThat(resultado.tieneMiembro(miembro3)).isFalse();
    }

    @Test
    void ejecutar_grupoNoExiste_lanzaEntityNotFound() {
        UUID grupoId = UUID.randomUUID();
        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.empty());

        RemoverMiembroUseCase useCase = new RemoverMiembroUseCase(grupoRepository);

        assertThatThrownBy(() -> useCase.ejecutar(grupoId, UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void ejecutar_actorNoEsAdmin_lanzaUnauthorized() {
        UUID grupoId = UUID.randomUUID();
        UUID creadorId = UUID.randomUUID();
        UUID miembro2 = UUID.randomUUID();
        UUID miembro3 = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", creadorId);
        grupo.agregarMiembro(miembro2);
        grupo.agregarMiembro(miembro3);

        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));

        RemoverMiembroUseCase useCase = new RemoverMiembroUseCase(grupoRepository);

        assertThatThrownBy(() -> useCase.ejecutar(grupoId, miembro2, miembro3))
                .isInstanceOf(UnauthorizedOperationException.class);
    }
}
