package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.PasswordHasher;
import com.divvy.autenticacion.domain.TokenGenerator;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import com.divvy.shared.domain.exception.InvalidCredentialsException;

public class LoginUseCase {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos";

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;

    public LoginUseCase(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher, TokenGenerator tokenGenerator) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
    }

    public String ejecutar(String email, String passwordPlano) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException(MENSAJE_CREDENCIALES_INVALIDAS));

        if (!passwordHasher.coincide(passwordPlano, usuario.passwordHash())) {
            throw new InvalidCredentialsException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        return tokenGenerator.generar(usuario.id());
    }
}
