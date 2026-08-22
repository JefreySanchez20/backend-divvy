package com.divvy.grupos.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Miembro {

    private final UUID usuarioId;
    private final Rol rol;
    private final Instant fechaIngreso;

    public Miembro(UUID usuarioId, Rol rol, Instant fechaIngreso) {
        this.usuarioId = Objects.requireNonNull(usuarioId, "El usuarioId del miembro no puede ser nulo");
        this.rol = Objects.requireNonNull(rol, "El rol del miembro no puede ser nulo");
        this.fechaIngreso = Objects.requireNonNull(fechaIngreso, "La fecha de ingreso no puede ser nula");
    }

    public UUID usuarioId() {
        return usuarioId;
    }

    public Rol rol() {
        return rol;
    }

    public Instant fechaIngreso() {
        return fechaIngreso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Miembro otro)) return false;
        return usuarioId.equals(otro.usuarioId) && rol == otro.rol && fechaIngreso.equals(otro.fechaIngreso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, rol, fechaIngreso);
    }

    @Override
    public String toString() {
        return "Miembro{usuarioId=%s, rol=%s, fechaIngreso=%s}".formatted(usuarioId, rol, fechaIngreso);
    }
}
