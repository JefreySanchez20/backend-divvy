package com.divvy.autenticacion.domain;

import com.divvy.shared.domain.exception.InvariantViolationException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class TokenRecuperacion {

    private static final String ALFABETO_CODIGO = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int LONGITUD_CODIGO = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UUID id;
    private final UUID usuarioId;
    private final String token;
    private final Instant fechaExpiracion;
    private boolean usado;

    private TokenRecuperacion(UUID id, UUID usuarioId, String token, Instant fechaExpiracion, boolean usado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    public static TokenRecuperacion generar(UUID id, UUID usuarioId, Duration validez) {
        Objects.requireNonNull(id, "El id no puede ser nulo");
        Objects.requireNonNull(usuarioId, "El usuarioId no puede ser nulo");
        Objects.requireNonNull(validez, "La validez no puede ser nula");
        String token = generarCodigo();
        Instant fechaExpiracion = Instant.now().plus(validez);
        return new TokenRecuperacion(id, usuarioId, token, fechaExpiracion, false);
    }

    private static String generarCodigo() {
        StringBuilder codigo = new StringBuilder(LONGITUD_CODIGO);
        for (int i = 0; i < LONGITUD_CODIGO; i++) {
            codigo.append(ALFABETO_CODIGO.charAt(RANDOM.nextInt(ALFABETO_CODIGO.length())));
        }
        return codigo.toString();
    }

    public static TokenRecuperacion reconstruir(UUID id, UUID usuarioId, String token, Instant fechaExpiracion, boolean usado) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(usuarioId);
        Objects.requireNonNull(token);
        Objects.requireNonNull(fechaExpiracion);
        return new TokenRecuperacion(id, usuarioId, token, fechaExpiracion, usado);
    }

    public boolean estaVigente() {
        return !usado && Instant.now().isBefore(fechaExpiracion);
    }

    public void marcarComoUsado() {
        if (!estaVigente()) {
            throw new InvariantViolationException("El token de recuperación es inválido o ya expiró");
        }
        this.usado = true;
    }

    public UUID id() {
        return id;
    }

    public UUID usuarioId() {
        return usuarioId;
    }

    public String token() {
        return token;
    }

    public Instant fechaExpiracion() {
        return fechaExpiracion;
    }

    public boolean usado() {
        return usado;
    }
}
