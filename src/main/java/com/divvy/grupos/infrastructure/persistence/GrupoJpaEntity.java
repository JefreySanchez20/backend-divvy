package com.divvy.grupos.infrastructure.persistence;

import com.divvy.grupos.domain.EstadoGrupo;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "grupos")
public class GrupoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoGrupo estado;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "grupo_miembros", joinColumns = @JoinColumn(name = "grupo_id"))
    private List<MiembroEmbeddable> miembros = new ArrayList<>();

    protected GrupoJpaEntity() {
    }

    public GrupoJpaEntity(UUID id, String nombre, Instant fechaCreacion, EstadoGrupo estado, List<MiembroEmbeddable> miembros) {
        this.id = id;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.miembros = miembros;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public EstadoGrupo getEstado() {
        return estado;
    }

    public List<MiembroEmbeddable> getMiembros() {
        return miembros;
    }
}
