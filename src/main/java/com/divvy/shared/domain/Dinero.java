package com.divvy.shared.domain;

import com.divvy.shared.domain.exception.InvariantViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Dinero {

    private final BigDecimal monto;
    private final String moneda;

    private Dinero(BigDecimal monto, String moneda) {
        this.monto = monto;
        this.moneda = moneda;
    }

    public static Dinero de(BigDecimal monto, String moneda) {
        Objects.requireNonNull(monto, "El monto no puede ser nulo");
        Objects.requireNonNull(moneda, "La moneda no puede ser nula");

        Currency currency = resolverMoneda(moneda);
        BigDecimal montoEscalado = monto.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
        return new Dinero(montoEscalado, moneda);
    }

    public static Dinero cero(String moneda) {
        return de(BigDecimal.ZERO, moneda);
    }

    private static Currency resolverMoneda(String moneda) {
        try {
            return Currency.getInstance(moneda);
        } catch (IllegalArgumentException e) {
            throw new InvariantViolationException("Código de moneda inválido (ISO 4217): " + moneda);
        }
    }

    public Dinero sumar(Dinero otro) {
        validarMismaMoneda(otro);
        return new Dinero(this.monto.add(otro.monto), this.moneda);
    }

    public Dinero restar(Dinero otro) {
        validarMismaMoneda(otro);
        return new Dinero(this.monto.subtract(otro.monto), this.moneda);
    }

    public Dinero multiplicar(BigDecimal factor) {
        Objects.requireNonNull(factor, "El factor no puede ser nulo");
        return new Dinero(this.monto.multiply(factor).setScale(this.monto.scale(), RoundingMode.HALF_UP), this.moneda);
    }

    public int comparar(Dinero otro) {
        validarMismaMoneda(otro);
        return this.monto.compareTo(otro.monto);
    }

    public boolean esPositivo() {
        return monto.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean esNegativo() {
        return monto.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean esCero() {
        return monto.compareTo(BigDecimal.ZERO) == 0;
    }

    private void validarMismaMoneda(Dinero otro) {
        Objects.requireNonNull(otro, "El monto a comparar no puede ser nulo");
        if (!this.moneda.equals(otro.moneda)) {
            throw new InvariantViolationException(
                    "No se pueden operar montos de distinta moneda: " + this.moneda + " vs " + otro.moneda);
        }
    }

    public BigDecimal monto() {
        return monto;
    }

    public String moneda() {
        return moneda;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dinero otro)) return false;
        return monto.compareTo(otro.monto) == 0 && moneda.equals(otro.moneda);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monto.stripTrailingZeros(), moneda);
    }

    @Override
    public String toString() {
        return monto + " " + moneda;
    }
}
