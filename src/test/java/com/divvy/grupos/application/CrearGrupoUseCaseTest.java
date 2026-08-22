package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearGrupoUseCaseTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Test
    void ejecutar_construyeGrupoYLoGuarda() {
        when(grupoRepository.guardar(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CrearGrupoUseCase useCase = new CrearGrupoUseCase(grupoRepository);
        UUID creadorId = UUID.randomUUID();

        Grupo resultado = useCase.ejecutar("Roomies", creadorId);

        assertThat(resultado.nombre()).isEqualTo("Roomies");
        assertThat(resultado.esAdmin(creadorId)).isTrue();

        ArgumentCaptor<Grupo> captor = ArgumentCaptor.forClass(Grupo.class);
        verify(grupoRepository).guardar(captor.capture());
        assertThat(captor.getValue().miembros()).hasSize(1);
    }
}
