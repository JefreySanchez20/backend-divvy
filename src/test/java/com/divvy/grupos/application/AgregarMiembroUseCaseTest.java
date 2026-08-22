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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgregarMiembroUseCaseTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Test
    void ejecutar_grupoExiste_agregaMiembroYGuarda() {
        UUID grupoId = UUID.randomUUID();
        UUID creadorId = UUID.randomUUID();
        UUID nuevoUsuario = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", creadorId);

        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));
        when(grupoRepository.guardar(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgregarMiembroUseCase useCase = new AgregarMiembroUseCase(grupoRepository);
        Grupo resultado = useCase.ejecutar(grupoId, nuevoUsuario);

        assertThat(resultado.tieneMiembro(nuevoUsuario)).isTrue();
    }

    @Test
    void ejecutar_grupoNoExiste_lanzaEntityNotFound() {
        UUID grupoId = UUID.randomUUID();
        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.empty());

        AgregarMiembroUseCase useCase = new AgregarMiembroUseCase(grupoRepository);

        assertThatThrownBy(() -> useCase.ejecutar(grupoId, UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
