package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.PasswordHasher;
import com.divvy.autenticacion.domain.TokenRecuperacion;
import com.divvy.autenticacion.domain.TokenRecuperacionRepository;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.InvariantViolationException;

public class RestablecerPasswordUseCase {

    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;

    public RestablecerPasswordUseCase(
            TokenRecuperacionRepository tokenRecuperacionRepository,
            UsuarioRepository usuarioRepository,
            PasswordHasher passwordHasher
    ) {
        this.tokenRecuperacionRepository = tokenRecuperacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
    }

    public void ejecutar(String token, String nuevoPasswordPlano) {
        TokenRecuperacion tokenRecuperacion = tokenRecuperacionRepository.buscarPorToken(token)
                .orElseThrow(() -> new InvariantViolationException("El token de recuperación es inválido o ya expiró"));

        tokenRecuperacion.marcarComoUsado();

        Usuario usuario = usuarioRepository.buscarPorId(tokenRecuperacion.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

        usuario.cambiarPassword(passwordHasher.hash(nuevoPasswordPlano));

        usuarioRepository.guardar(usuario);
        tokenRecuperacionRepository.guardar(tokenRecuperacion);
    }
}
