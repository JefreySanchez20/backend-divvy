package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarUsuarioPorEmailUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void ejecutar_emailExiste_devuelveUsuario() {
        Usuario usuario = Usuario.reconstruir(UUID.randomUUID(), "ana@demo.com", "hash-seguro", "Ana", Instant.now());
        when(usuarioRepository.buscarPorEmail("ana@demo.com")).thenReturn(Optional.of(usuario));

        BuscarUsuarioPorEmailUseCase useCase = new BuscarUsuarioPorEmailUseCase(usuarioRepository);

        assertThat(useCase.ejecutar("ana@demo.com")).isEqualTo(usuario);
    }

    @Test
    void ejecutar_emailNoExiste_lanzaEntityNotFound() {
        when(usuarioRepository.buscarPorEmail("nadie@demo.com")).thenReturn(Optional.empty());

        BuscarUsuarioPorEmailUseCase useCase = new BuscarUsuarioPorEmailUseCase(usuarioRepository);

        assertThatThrownBy(() -> useCase.ejecutar("nadie@demo.com"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
