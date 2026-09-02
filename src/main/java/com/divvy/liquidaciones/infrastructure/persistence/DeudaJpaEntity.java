package com.divvy.liquidaciones.infrastructure.persistence;

import com.divvy.liquidaciones.domain.EstadoDeuda;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deudas")
public class DeudaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "deudor_id", nullable = false)
    private UUID deudorId;

    @Column(name = "acreedor_id", nullable = false)
    private UUID acreedorId;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false)
    private String moneda;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoDeuda estado;

    @Column(name = "fecha_pago")
    private Instant fechaPago;

    protected DeudaJpaEntity() {
    }

    public DeudaJpaEntity(
            UUID id, UUID deudorId, UUID acreedorId, BigDecimal monto, String moneda, EstadoDeuda estado, Instant fechaPago
    ) {
        this.id = id;
        this.deudorId = deudorId;
        this.acreedorId = acreedorId;
        this.monto = monto;
        this.moneda = moneda;
        this.estado = estado;
        this.fechaPago = fechaPago;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDeudorId() {
        return deudorId;
    }

    public UUID getAcreedorId() {
        return acreedorId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public EstadoDeuda getEstado() {
        return estado;
    }

    public Instant getFechaPago() {
        return fechaPago;
    }
}
