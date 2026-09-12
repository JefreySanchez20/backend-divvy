package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;

import java.util.List;
import java.util.UUID;

public class BuscarUsuariosPorIdsUseCase {

    private final UsuarioRepository usuarioRepository;

    public BuscarUsuariosPorIdsUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> ejecutar(List<UUID> ids) {
        return usuarioRepository.buscarPorIds(ids);
    }
}
