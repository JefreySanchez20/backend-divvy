package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.EmailSender;
import com.divvy.autenticacion.domain.TokenRecuperacion;
import com.divvy.autenticacion.domain.TokenRecuperacionRepository;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitarRecuperacionPasswordUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenRecuperacionRepository tokenRecuperacionRepository;

    @Mock
    private EmailSender emailSender;

    @Test
    void ejecutar_emailExiste_generaTokenYEnviaCorreo() {
        Usuario usuario = Usuario.reconstruir(UUID.randomUUID(), "ana@demo.com", "hash", "Ana", Instant.now());
        when(usuarioRepository.buscarPorEmail("ana@demo.com")).thenReturn(Optional.of(usuario));
        when(tokenRecuperacionRepository.guardar(any(TokenRecuperacion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SolicitarRecuperacionPasswordUseCase useCase = new SolicitarRecuperacionPasswordUseCase(
                usuarioRepository, tokenRecuperacionRepository, emailSender, Duration.ofMinutes(30));

        useCase.ejecutar("ana@demo.com");

        ArgumentCaptor<TokenRecuperacion> tokenCaptor = ArgumentCaptor.forClass(TokenRecuperacion.class);
        verify(tokenRecuperacionRepository).guardar(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().usuarioId()).isEqualTo(usuario.id());

        verify(emailSender).enviar(org.mockito.ArgumentMatchers.eq("ana@demo.com"), any(), any());
    }

    @Test
    void ejecutar_emailNoExiste_noHaceNadaNiRevelaInformacion() {
        when(usuarioRepository.buscarPorEmail("nadie@demo.com")).thenReturn(Optional.empty());

        SolicitarRecuperacionPasswordUseCase useCase = new SolicitarRecuperacionPasswordUseCase(
                usuarioRepository, tokenRecuperacionRepository, emailSender, Duration.ofMinutes(30));

        useCase.ejecutar("nadie@demo.com");

        verify(tokenRecuperacionRepository, never()).guardar(any());
        verifyNoInteractions(emailSender);
    }
}
