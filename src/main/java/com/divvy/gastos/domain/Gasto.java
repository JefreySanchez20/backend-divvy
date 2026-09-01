package com.divvy.gastos.domain;

import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.exception.InvariantViolationException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Gasto {

    private final UUID id;
    private final UUID grupoId;
    private String descripcion;
    private Dinero monto;
    private UUID pagadoPor;
    private Instant fecha;
    private String categoria;
    private DivisionGasto division;

    private Gasto(UUID id, UUID grupoId, String descripcion, Dinero monto, UUID pagadoPor, Instant fecha, String categoria, DivisionGasto division) {
        this.id = id;
        this.grupoId = grupoId;
        this.descripcion = descripcion;
        this.monto = monto;
        this.pagadoPor = pagadoPor;
        this.fecha = fecha;
        this.categoria = categoria;
        this.division = division;
    }

    public static Gasto registrar(
            UUID id, UUID grupoId, String descripcion, Dinero monto, UUID pagadoPor,
            Instant fecha, String categoria, DivisionGasto division
    ) {
        Objects.requireNonNull(id, "El id no puede ser nulo");
        Objects.requireNonNull(grupoId, "El grupoId no puede ser nulo");
        Objects.requireNonNull(pagadoPor, "El pagadoPor no puede ser nulo");
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        validarDescripcion(descripcion);
        validarMonto(monto);
        validarDivisionCoincideConMonto(monto, division);
        return new Gasto(id, grupoId, descripcion, monto, pagadoPor, fecha, categoria, division);
    }

    public static Gasto reconstruir(
            UUID id, UUID grupoId, String descripcion, Dinero monto, UUID pagadoPor,
            Instant fecha, String categoria, DivisionGasto division
    ) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(grupoId);
        Objects.requireNonNull(descripcion);
        Objects.requireNonNull(monto);
        Objects.requireNonNull(pagadoPor);
        Objects.requireNonNull(fecha);
        Objects.requireNonNull(division);
        return new Gasto(id, grupoId, descripcion, monto, pagadoPor, fecha, categoria, division);
    }

    public void editar(String descripcion, Dinero monto, UUID pagadoPor, Instant fecha, String categoria, DivisionGasto division) {
        Objects.requireNonNull(pagadoPor, "El pagadoPor no puede ser nulo");
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        validarDescripcion(descripcion);
        validarMonto(monto);
        validarDivisionCoincideConMonto(monto, division);
        this.descripcion = descripcion;
        this.monto = monto;
        this.pagadoPor = pagadoPor;
        this.fecha = fecha;
        this.categoria = categoria;
        this.division = division;
    }

    private static void validarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new InvariantViolationException("La descripción del gasto no puede estar vacía");
        }
    }

    private static void validarMonto(Dinero monto) {
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        if (!monto.esPositivo()) {
            throw new InvariantViolationException("El monto del gasto debe ser mayor a cero");
        }
    }

    private static void validarDivisionCoincideConMonto(Dinero monto, DivisionGasto division) {
        Objects.requireNonNull(division, "La división no puede ser nula");
        var suma = division.detalle().values().stream()
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        if (suma.compareTo(monto.monto()) != 0) {
            throw new InvariantViolationException("La suma de las divisiones no coincide con el monto total del gasto");
        }
    }

    public UUID id() {
        return id;
    }

    public UUID grupoId() {
        return grupoId;
    }

    public String descripcion() {
        return descripcion;
    }

    public Dinero monto() {
        return monto;
    }

    public UUID pagadoPor() {
        return pagadoPor;
    }

    public Instant fecha() {
        return fecha;
    }

    public String categoria() {
        return categoria;
    }

    public DivisionGasto division() {
        return division;
    }
}
