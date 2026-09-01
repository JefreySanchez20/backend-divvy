package com.divvy.autenticacion.infrastructure.security;

import com.divvy.autenticacion.domain.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordHasher implements PasswordHasher {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String passwordPlano) {
        return encoder.encode(passwordPlano);
    }

    @Override
    public boolean coincide(String passwordPlano, String passwordHash) {
        return encoder.matches(passwordPlano, passwordHash);
    }
}
