package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarGruposDelUsuarioUseCaseTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Test
    void ejecutar_delegaEnElRepositorio() {
        UUID usuarioId = UUID.randomUUID();
        Grupo grupo1 = Grupo.crear(UUID.randomUUID(), "Roomies", usuarioId);
        Grupo grupo2 = Grupo.crear(UUID.randomUUID(), "Viaje a Cusco", usuarioId);
        when(grupoRepository.buscarPorUsuario(usuarioId)).thenReturn(List.of(grupo1, grupo2));

        ListarGruposDelUsuarioUseCase useCase = new ListarGruposDelUsuarioUseCase(grupoRepository);

        assertThat(useCase.ejecutar(usuarioId)).containsExactly(grupo1, grupo2);
    }
}
