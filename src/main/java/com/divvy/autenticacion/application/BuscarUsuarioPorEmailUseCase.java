package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;

public class BuscarUsuarioPorEmailUseCase {

    private final UsuarioRepository usuarioRepository;

    public BuscarUsuarioPorEmailUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario ejecutar(String email) {
        return usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario registrado con ese email"));
    }
}
