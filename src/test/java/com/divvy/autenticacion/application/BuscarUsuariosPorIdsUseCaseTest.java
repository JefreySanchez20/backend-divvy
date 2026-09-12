package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarUsuariosPorIdsUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void ejecutar_idsExisten_devuelveUsuariosEncontrados() {
        Usuario ana = Usuario.reconstruir(UUID.randomUUID(), "ana@demo.com", "hash-seguro", "Ana", Instant.now());
        Usuario beto = Usuario.reconstruir(UUID.randomUUID(), "beto@demo.com", "hash-seguro", "Beto", Instant.now());
        List<UUID> ids = List.of(ana.id(), beto.id());
        when(usuarioRepository.buscarPorIds(ids)).thenReturn(List.of(ana, beto));

        BuscarUsuariosPorIdsUseCase useCase = new BuscarUsuariosPorIdsUseCase(usuarioRepository);

        assertThat(useCase.ejecutar(ids)).containsExactly(ana, beto);
    }

    @Test
    void ejecutar_algunIdNoExiste_devuelveSoloLosEncontrados() {
        Usuario ana = Usuario.reconstruir(UUID.randomUUID(), "ana@demo.com", "hash-seguro", "Ana", Instant.now());
        UUID idInexistente = UUID.randomUUID();
        List<UUID> ids = List.of(ana.id(), idInexistente);
        when(usuarioRepository.buscarPorIds(ids)).thenReturn(List.of(ana));

        BuscarUsuariosPorIdsUseCase useCase = new BuscarUsuariosPorIdsUseCase(usuarioRepository);

        assertThat(useCase.ejecutar(ids)).containsExactly(ana);
    }
}
