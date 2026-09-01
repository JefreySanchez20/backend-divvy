package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.PasswordHasher;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import com.divvy.shared.domain.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Test
    void ejecutar_emailNuevo_hasheaYGuardaElUsuario() {
        when(usuarioRepository.buscarPorEmail("ana@demo.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("clave123")).thenReturn("hash-seguro");
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(usuarioRepository, passwordHasher);
        Usuario resultado = useCase.ejecutar("ana@demo.com", "clave123", "Ana");

        assertThat(resultado.email()).isEqualTo("ana@demo.com");
        assertThat(resultado.passwordHash()).isEqualTo("hash-seguro");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        org.mockito.Mockito.verify(usuarioRepository).guardar(captor.capture());
        assertThat(captor.getValue().nombre()).isEqualTo("Ana");
    }

    @Test
    void ejecutar_emailYaRegistrado_lanzaInvariantViolation() {
        when(usuarioRepository.buscarPorEmail("ana@demo.com"))
                .thenReturn(Optional.of(Usuario.registrar(java.util.UUID.randomUUID(), "ana@demo.com", "hash", "Ana")));

        RegistrarUsuarioUseCase useCase = new RegistrarUsuarioUseCase(usuarioRepository, passwordHasher);

        assertThatThrownBy(() -> useCase.ejecutar("ana@demo.com", "clave123", "Ana"))
                .isInstanceOf(InvariantViolationException.class);
    }
}
