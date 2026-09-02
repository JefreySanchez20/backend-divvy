package com.divvy.liquidaciones.domain;

import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Deuda {

    private final UUID id;
    private final UUID deudorId;
    private final UUID acreedorId;
    private final Dinero monto;
    private EstadoDeuda estado;
    private Instant fechaPago;

    private Deuda(UUID id, UUID deudorId, UUID acreedorId, Dinero monto, EstadoDeuda estado, Instant fechaPago) {
        this.id = id;
        this.deudorId = deudorId;
        this.acreedorId = acreedorId;
        this.monto = monto;
        this.estado = estado;
        this.fechaPago = fechaPago;
    }

    public static Deuda crear(UUID id, UUID deudorId, UUID acreedorId, Dinero monto) {
        Objects.requireNonNull(id, "El id no puede ser nulo");
        Objects.requireNonNull(deudorId, "El deudorId no puede ser nulo");
        Objects.requireNonNull(acreedorId, "El acreedorId no puede ser nulo");
        validarMonto(monto);
        validarDeudorDistintoDeAcreedor(deudorId, acreedorId);
        return new Deuda(id, deudorId, acreedorId, monto, EstadoDeuda.PENDIENTE, null);
    }

    public static Deuda reconstruir(
            UUID id, UUID deudorId, UUID acreedorId, Dinero monto, EstadoDeuda estado, Instant fechaPago
    ) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(deudorId);
        Objects.requireNonNull(acreedorId);
        Objects.requireNonNull(monto);
        Objects.requireNonNull(estado);
        return new Deuda(id, deudorId, acreedorId, monto, estado, fechaPago);
    }

    private static void validarMonto(Dinero monto) {
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        if (!monto.esPositivo()) {
            throw new InvariantViolationException("El monto de la deuda debe ser mayor a cero");
        }
    }

    private static void validarDeudorDistintoDeAcreedor(UUID deudorId, UUID acreedorId) {
        if (deudorId.equals(acreedorId)) {
            throw new InvariantViolationException("El deudor y el acreedor no pueden ser el mismo usuario");
        }
    }

    public void marcarComoPagada() {
        if (estado == EstadoDeuda.PAGADA) {
            throw new InvariantViolationException("La deuda ya fue pagada");
        }
        this.estado = EstadoDeuda.PAGADA;
        this.fechaPago = Instant.now();
    }

    public UUID id() {
        return id;
    }

    public UUID deudorId() {
        return deudorId;
    }

    public UUID acreedorId() {
        return acreedorId;
    }

    public Dinero monto() {
        return monto;
    }

    public EstadoDeuda estado() {
        return estado;
    }

    public Instant fechaPago() {
        return fechaPago;
    }
}
