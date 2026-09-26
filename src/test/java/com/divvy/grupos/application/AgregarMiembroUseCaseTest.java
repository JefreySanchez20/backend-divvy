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
import com.divvy.shared.domain.exception.UnauthorizedOperationException;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AgregarMiembroUseCaseTest {

    @Mock
    private GrupoRepository grupoRepository;

    @Test
    void ejecutar_grupoExiste_agregaMiembroYGuarda() {
        UUID grupoId = UUID.randomUUID();
        UUID creadorId = UUID.randomUUID();
        UUID nuevoUsuario = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", "PEN", creadorId);

        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));
        when(grupoRepository.guardar(any(Grupo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgregarMiembroUseCase useCase = new AgregarMiembroUseCase(grupoRepository);
        Grupo resultado = useCase.ejecutar(grupoId, creadorId, nuevoUsuario);

        assertThat(resultado.tieneMiembro(nuevoUsuario)).isTrue();
    }

    @Test
    void ejecutar_grupoNoExiste_lanzaEntityNotFound() {
        UUID grupoId = UUID.randomUUID();
        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.empty());

        AgregarMiembroUseCase useCase = new AgregarMiembroUseCase(grupoRepository);

        assertThatThrownBy(() -> useCase.ejecutar(grupoId, UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void ejecutar_actorNoEsMiembro_lanzaUnauthorizedYNoGuarda() {
        UUID grupoId = UUID.randomUUID();
        UUID intruso = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", "PEN", UUID.randomUUID());
        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));

        AgregarMiembroUseCase useCase = new AgregarMiembroUseCase(grupoRepository);

        // Un desconocido no puede meterse a sí mismo (ni a otros) en un grupo ajeno.
        assertThatThrownBy(() -> useCase.ejecutar(grupoId, intruso, intruso))
                .isInstanceOf(UnauthorizedOperationException.class);
        verify(grupoRepository, never()).guardar(any(Grupo.class));
        assertThat(grupo.tieneMiembro(intruso)).isFalse();
    }

    @Test
    void ejecutar_actorEsMiembroSinRolAdmin_lanzaUnauthorized() {
        UUID grupoId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID miembroId = UUID.randomUUID();
        Grupo grupo = Grupo.crear(grupoId, "Roomies", "PEN", adminId);
        grupo.agregarMiembro(miembroId);
        when(grupoRepository.buscarPorId(grupoId)).thenReturn(Optional.of(grupo));

        AgregarMiembroUseCase useCase = new AgregarMiembroUseCase(grupoRepository);

        assertThatThrownBy(() -> useCase.ejecutar(grupoId, miembroId, UUID.randomUUID()))
                .isInstanceOf(UnauthorizedOperationException.class);
        verify(grupoRepository, never()).guardar(any(Grupo.class));
    }
}
