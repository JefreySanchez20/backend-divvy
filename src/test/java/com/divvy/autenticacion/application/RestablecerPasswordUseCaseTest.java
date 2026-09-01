package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.PasswordHasher;
import com.divvy.autenticacion.domain.TokenRecuperacion;
import com.divvy.autenticacion.domain.TokenRecuperacionRepository;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import com.divvy.shared.domain.exception.InvariantViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestablecerPasswordUseCaseTest {

    @Mock
    private TokenRecuperacionRepository tokenRecuperacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Test
    void ejecutar_tokenVigente_cambiaPasswordYMarcaTokenComoUsado() {
        UUID usuarioId = UUID.randomUUID();
        Usuario usuario = Usuario.reconstruir(usuarioId, "ana@demo.com", "hash-viejo", "Ana", Instant.now());
        TokenRecuperacion token = TokenRecuperacion.generar(UUID.randomUUID(), usuarioId, Duration.ofMinutes(30));

        when(tokenRecuperacionRepository.buscarPorToken(token.token())).thenReturn(Optional.of(token));
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(passwordHasher.hash("nuevaClave123")).thenReturn("hash-nuevo");
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRecuperacionRepository.guardar(any(TokenRecuperacion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RestablecerPasswordUseCase useCase = new RestablecerPasswordUseCase(
                tokenRecuperacionRepository, usuarioRepository, passwordHasher);

        useCase.ejecutar(token.token(), "nuevaClave123");

        assertThat(usuario.passwordHash()).isEqualTo("hash-nuevo");
        assertThat(token.usado()).isTrue();
        verify(usuarioRepository).guardar(usuario);
        verify(tokenRecuperacionRepository).guardar(token);
    }

    @Test
    void ejecutar_tokenNoExiste_lanzaInvariantViolation() {
        when(tokenRecuperacionRepository.buscarPorToken("no-existe")).thenReturn(Optional.empty());

        RestablecerPasswordUseCase useCase = new RestablecerPasswordUseCase(
                tokenRecuperacionRepository, usuarioRepository, passwordHasher);

        assertThatThrownBy(() -> useCase.ejecutar("no-existe", "nuevaClave123"))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void ejecutar_tokenExpirado_lanzaInvariantViolation() {
        TokenRecuperacion tokenExpirado = TokenRecuperacion.reconstruir(
                UUID.randomUUID(), UUID.randomUUID(), "token-expirado", Instant.now().minusSeconds(60), false);
        when(tokenRecuperacionRepository.buscarPorToken("token-expirado")).thenReturn(Optional.of(tokenExpirado));

        RestablecerPasswordUseCase useCase = new RestablecerPasswordUseCase(
                tokenRecuperacionRepository, usuarioRepository, passwordHasher);

        assertThatThrownBy(() -> useCase.ejecutar("token-expirado", "nuevaClave123"))
                .isInstanceOf(InvariantViolationException.class);
    }

    @Test
    void ejecutar_tokenYaUsado_lanzaInvariantViolation() {
        TokenRecuperacion tokenUsado = TokenRecuperacion.reconstruir(
                UUID.randomUUID(), UUID.randomUUID(), "token-usado", Instant.now().plusSeconds(600), true);
        when(tokenRecuperacionRepository.buscarPorToken("token-usado")).thenReturn(Optional.of(tokenUsado));

        RestablecerPasswordUseCase useCase = new RestablecerPasswordUseCase(
                tokenRecuperacionRepository, usuarioRepository, passwordHasher);

        assertThatThrownBy(() -> useCase.ejecutar("token-usado", "nuevaClave123"))
                .isInstanceOf(InvariantViolationException.class);
    }
}
