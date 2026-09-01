package com.divvy.gastos.infrastructure.persistence;

import com.divvy.gastos.domain.TipoDivision;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "gastos")
public class GastoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "grupo_id", nullable = false)
    private UUID grupoId;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false)
    private String moneda;

    @Column(name = "pagado_por", nullable = false)
    private UUID pagadoPor;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Column(name = "categoria")
    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_division", nullable = false)
    private TipoDivision tipoDivision;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "gasto_division_detalle", joinColumns = @JoinColumn(name = "gasto_id"))
    @MapKeyColumn(name = "usuario_id")
    @Column(name = "valor", nullable = false)
    private Map<UUID, BigDecimal> detalle = new LinkedHashMap<>();

    protected GastoJpaEntity() {
    }

    public GastoJpaEntity(
            UUID id, UUID grupoId, String descripcion, BigDecimal monto, String moneda, UUID pagadoPor,
            Instant fecha, String categoria, TipoDivision tipoDivision, Map<UUID, BigDecimal> detalle
    ) {
        this.id = id;
        this.grupoId = grupoId;
        this.descripcion = descripcion;
        this.monto = monto;
        this.moneda = moneda;
        this.pagadoPor = pagadoPor;
        this.fecha = fecha;
        this.categoria = categoria;
        this.tipoDivision = tipoDivision;
        this.detalle = detalle;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGrupoId() {
        return grupoId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public UUID getPagadoPor() {
        return pagadoPor;
    }

    public Instant getFecha() {
        return fecha;
    }

    public String getCategoria() {
        return categoria;
    }

    public TipoDivision getTipoDivision() {
        return tipoDivision;
    }

    public Map<UUID, BigDecimal> getDetalle() {
        return detalle;
    }
}
