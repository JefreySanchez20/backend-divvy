package com.divvy.liquidaciones.infrastructure.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "liquidaciones")
public class LiquidacionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "grupo_id", nullable = false)
    private UUID grupoId;

    @Column(name = "fecha_calculo", nullable = false)
    private Instant fechaCalculo;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "liquidacion_id", nullable = false)
    private List<DeudaJpaEntity> deudas = new ArrayList<>();

    protected LiquidacionJpaEntity() {
    }

    public LiquidacionJpaEntity(UUID id, UUID grupoId, Instant fechaCalculo, List<DeudaJpaEntity> deudas) {
        this.id = id;
        this.grupoId = grupoId;
        this.fechaCalculo = fechaCalculo;
        this.deudas = deudas;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGrupoId() {
        return grupoId;
    }

    public Instant getFechaCalculo() {
        return fechaCalculo;
    }

    public List<DeudaJpaEntity> getDeudas() {
        return deudas;
    }
}
