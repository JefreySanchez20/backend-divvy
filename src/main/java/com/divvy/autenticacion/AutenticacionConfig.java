package com.divvy.autenticacion;

import com.divvy.autenticacion.application.LoginUseCase;
import com.divvy.autenticacion.application.RegistrarUsuarioUseCase;
import com.divvy.autenticacion.application.RestablecerPasswordUseCase;
import com.divvy.autenticacion.application.SolicitarRecuperacionPasswordUseCase;
import com.divvy.autenticacion.domain.EmailSender;
import com.divvy.autenticacion.domain.PasswordHasher;
import com.divvy.autenticacion.domain.TokenGenerator;
import com.divvy.autenticacion.domain.TokenRecuperacionRepository;
import com.divvy.autenticacion.domain.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AutenticacionConfig {

    @Bean
    public RegistrarUsuarioUseCase registrarUsuarioUseCase(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher) {
        return new RegistrarUsuarioUseCase(usuarioRepository, passwordHasher);
    }

    @Bean
    public LoginUseCase loginUseCase(UsuarioRepository usuarioRepository, PasswordHasher passwordHasher, TokenGenerator tokenGenerator) {
        return new LoginUseCase(usuarioRepository, passwordHasher, tokenGenerator);
    }

    @Bean
    public SolicitarRecuperacionPasswordUseCase solicitarRecuperacionPasswordUseCase(
            UsuarioRepository usuarioRepository,
            TokenRecuperacionRepository tokenRecuperacionRepository,
            EmailSender emailSender,
            @Value("${divvy.password-reset.expiration-minutes}") long expirationMinutes
    ) {
        return new SolicitarRecuperacionPasswordUseCase(
                usuarioRepository, tokenRecuperacionRepository, emailSender, Duration.ofMinutes(expirationMinutes));
    }

    @Bean
    public RestablecerPasswordUseCase restablecerPasswordUseCase(
            TokenRecuperacionRepository tokenRecuperacionRepository,
            UsuarioRepository usuarioRepository,
            PasswordHasher passwordHasher
    ) {
        return new RestablecerPasswordUseCase(tokenRecuperacionRepository, usuarioRepository, passwordHasher);
    }
}
