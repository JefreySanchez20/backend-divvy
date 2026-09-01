package com.divvy.shared.domain.events;

import com.divvy.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public final class GastoRegistradoEvent extends DomainEvent {

    private final UUID grupoId;
    private final UUID gastoId;
    private final BigDecimal monto;
    private final String moneda;
    private final Map<UUID, BigDecimal> division;

    public GastoRegistradoEvent(UUID grupoId, UUID gastoId, BigDecimal monto, String moneda, Map<UUID, BigDecimal> division) {
        this.grupoId = grupoId;
        this.gastoId = gastoId;
        this.monto = monto;
        this.moneda = moneda;
        this.division = Map.copyOf(division);
    }

    public UUID grupoId() {
        return grupoId;
    }

    public UUID gastoId() {
        return gastoId;
    }

    public BigDecimal monto() {
        return monto;
    }

    public String moneda() {
        return moneda;
    }

    public Map<UUID, BigDecimal> division() {
        return division;
    }
}
