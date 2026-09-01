package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.PasswordHasher;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import com.divvy.shared.domain.exception.InvariantViolationException;

import java.util.UUID;

public class RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public RegistrarUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    public Usuario ejecutar(String email, String passwordPlano, String nombre) {
        if (usuarioRepository.buscarPorEmail(email).isPresent()) {
            throw new InvariantViolationException("Ya existe una cuenta registrada con este email");
        }
        String passwordHash = passwordHasher.hash(passwordPlano);
        Usuario usuario = Usuario.registrar(UUID.randomUUID(), email, passwordHash, nombre);
        return usuarioRepository.guardar(usuario);
    }
}
