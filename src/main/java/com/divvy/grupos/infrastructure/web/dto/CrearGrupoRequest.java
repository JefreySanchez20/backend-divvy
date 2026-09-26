package com.divvy.grupos.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearGrupoRequest(
        @NotBlank(message = "name is required") String name,
        String currency
) {

    public static final String MONEDA_POR_DEFECTO = "PEN";

    /** La moneda es opcional para no romper clientes anteriores: sin ella el grupo usa PEN. */
    public String currencyOrDefault() {
        return currency == null || currency.isBlank() ? MONEDA_POR_DEFECTO : currency;
    }
}
