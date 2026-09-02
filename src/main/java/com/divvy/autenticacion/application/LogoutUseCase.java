package com.divvy.autenticacion.application;

import com.divvy.shared.domain.TokenBlacklist;

import java.time.Instant;

public class LogoutUseCase {

    private final TokenBlacklist tokenBlacklist;

    public LogoutUseCase(TokenBlacklist tokenBlacklist) {
        this.tokenBlacklist = tokenBlacklist;
    }

    public void ejecutar(String jti, Instant expiracion) {
        tokenBlacklist.revocar(jti, expiracion);
    }
}
