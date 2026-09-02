package com.divvy.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "revoked_tokens")
public class RevokedTokenJpaEntity {

    @Id
    private String jti;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    protected RevokedTokenJpaEntity() {
    }

    public RevokedTokenJpaEntity(String jti, Instant expiraEn) {
        this.jti = jti;
        this.expiraEn = expiraEn;
    }

    public String getJti() {
        return jti;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }
}
