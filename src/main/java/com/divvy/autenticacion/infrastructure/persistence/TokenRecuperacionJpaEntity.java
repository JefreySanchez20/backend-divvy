package com.divvy.autenticacion.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class TokenRecuperacionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @Column(name = "fecha_expiracion", nullable = false)
    private Instant fechaExpiracion;

    @Column(name = "usado", nullable = false)
    private boolean usado;

    protected TokenRecuperacionJpaEntity() {
    }

    public TokenRecuperacionJpaEntity(UUID id, UUID usuarioId, String token, Instant fechaExpiracion, boolean usado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getToken() {
        return token;
    }

    public Instant getFechaExpiracion() {
        return fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }
}
