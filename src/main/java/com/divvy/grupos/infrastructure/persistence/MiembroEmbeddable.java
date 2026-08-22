package com.divvy.grupos.infrastructure.persistence;

import com.divvy.grupos.domain.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;
import java.util.UUID;

@Embeddable
public class MiembroEmbeddable {

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    @Column(name = "fecha_ingreso", nullable = false)
    private Instant fechaIngreso;

    protected MiembroEmbeddable() {
    }

    public MiembroEmbeddable(UUID usuarioId, Rol rol, Instant fechaIngreso) {
        this.usuarioId = usuarioId;
        this.rol = rol;
        this.fechaIngreso = fechaIngreso;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public Rol getRol() {
        return rol;
    }

    public Instant getFechaIngreso() {
        return fechaIngreso;
    }
}
