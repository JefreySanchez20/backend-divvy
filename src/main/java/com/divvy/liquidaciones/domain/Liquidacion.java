package com.divvy.liquidaciones.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Liquidacion {

    private final UUID id;
    private final UUID grupoId;
    private final Instant fechaCalculo;
    private final List<Deuda> deudas;

    private Liquidacion(UUID id, UUID grupoId, Instant fechaCalculo, List<Deuda> deudas) {
        this.id = id;
        this.grupoId = grupoId;
        this.fechaCalculo = fechaCalculo;
        this.deudas = deudas;
    }

    public static Liquidacion calcular(UUID id, UUID grupoId, List<Deuda> deudas) {
        Objects.requireNonNull(id, "El id no puede ser nulo");
        Objects.requireNonNull(grupoId, "El grupoId no puede ser nulo");
        Objects.requireNonNull(deudas, "Las deudas no pueden ser nulas");
        return new Liquidacion(id, grupoId, Instant.now(), List.copyOf(deudas));
    }

    public static Liquidacion reconstruir(UUID id, UUID grupoId, Instant fechaCalculo, List<Deuda> deudas) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(grupoId);
        Objects.requireNonNull(fechaCalculo);
        Objects.requireNonNull(deudas);
        return new Liquidacion(id, grupoId, fechaCalculo, List.copyOf(deudas));
    }

    public Optional<Deuda> buscarDeuda(UUID deudaId) {
        return deudas.stream().filter(d -> d.id().equals(deudaId)).findFirst();
    }

    public UUID id() {
        return id;
    }

    public UUID grupoId() {
        return grupoId;
    }

    public Instant fechaCalculo() {
        return fechaCalculo;
    }

    public List<Deuda> deudas() {
        return deudas;
    }
}
