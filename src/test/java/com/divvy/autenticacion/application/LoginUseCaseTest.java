package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.PasswordHasher;
import com.divvy.autenticacion.domain.TokenGenerator;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import com.divvy.shared.domain.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenGenerator tokenGenerator;

    @Test
    void ejecutar_credencialesCorrectas_devuelveToken() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.reconstruir(usuarioId, "ana@demo.com", "hash-seguro", "Ana", java.time.Instant.now());

        when(usuarioRepository.buscarPorEmail("ana@demo.com")).thenReturn(Optional.of(usuario));
        when(passwordHasher.coincide("clave123", "hash-seguro")).thenReturn(true);
        when(tokenGenerator.generar(usuarioId)).thenReturn("token-jwt");

        LoginUseCase useCase = new LoginUseCase(usuarioRepository, passwordHasher, tokenGenerator);

        assertThat(useCase.ejecutar("ana@demo.com", "clave123")).isEqualTo("token-jwt");
    }

    @Test
    void ejecutar_emailNoExiste_lanzaInvalidCredentials() {
        when(usuarioRepository.buscarPorEmail("nadie@demo.com")).thenReturn(Optional.empty());

        LoginUseCase useCase = new LoginUseCase(usuarioRepository, passwordHasher, tokenGenerator);

        assertThatThrownBy(() -> useCase.ejecutar("nadie@demo.com", "clave123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void ejecutar_passwordIncorrecta_lanzaInvalidCredentials() {
        Usuario usuario = Usuario.reconstruir(UUID.randomUUID(), "ana@demo.com", "hash-seguro", "Ana", java.time.Instant.now());
        when(usuarioRepository.buscarPorEmail("ana@demo.com")).thenReturn(Optional.of(usuario));
        when(passwordHasher.coincide("claveIncorrecta", "hash-seguro")).thenReturn(false);

        LoginUseCase useCase = new LoginUseCase(usuarioRepository, passwordHasher, tokenGenerator);

        assertThatThrownBy(() -> useCase.ejecutar("ana@demo.com", "claveIncorrecta"))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
