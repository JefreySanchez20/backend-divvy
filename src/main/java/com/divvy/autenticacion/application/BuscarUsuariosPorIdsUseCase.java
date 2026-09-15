package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import com.divvy.shared.domain.exception.InvariantViolationException;

import java.util.List;
import java.util.UUID;

public class BuscarUsuariosPorIdsUseCase {

    public static final int MAX_IDS_POR_CONSULTA = 100;

    private final UsuarioRepository usuarioRepository;

    public BuscarUsuariosPorIdsUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> ejecutar(List<UUID> ids) {
        if (ids.size() > MAX_IDS_POR_CONSULTA) {
            throw new InvariantViolationException(
                    "No se pueden consultar más de %d ids por vez".formatted(MAX_IDS_POR_CONSULTA)
            );
        }
        return usuarioRepository.buscarPorIds(ids);
    }
}
