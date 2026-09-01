package com.divvy.autenticacion.domain;

import com.divvy.shared.domain.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Usuario {

    private final UUID id;
    private final String email;
    private String passwordHash;
    private final String nombre;
    private final Instant fechaCreacion;

    private Usuario(UUID id, String email, String passwordHash, String nombre, Instant fechaCreacion) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
    }

    public static Usuario registrar(UUID id, String email, String passwordHash, String nombre) {
        Objects.requireNonNull(id, "El id no puede ser nulo");
        validarEmail(email);
        validarPasswordHash(passwordHash);
        validarNombre(nombre);
        return new Usuario(id, email, passwordHash, nombre, Instant.now());
    }

    public static Usuario reconstruir(UUID id, String email, String passwordHash, String nombre, Instant fechaCreacion) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(email);
        Objects.requireNonNull(passwordHash);
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(fechaCreacion);
        return new Usuario(id, email, passwordHash, nombre, fechaCreacion);
    }

    private static void validarEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new InvariantViolationException("El email no es válido");
        }
    }

    private static void validarPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvariantViolationException("El hash de la contraseña no puede estar vacío");
        }
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new InvariantViolationException("El nombre no puede estar vacío");
        }
    }

    public void cambiarPassword(String nuevoPasswordHash) {
        validarPasswordHash(nuevoPasswordHash);
        this.passwordHash = nuevoPasswordHash;
    }

    public UUID id() {
        return id;
    }

    public String email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public String nombre() {
        return nombre;
    }

    public Instant fechaCreacion() {
        return fechaCreacion;
    }
}
