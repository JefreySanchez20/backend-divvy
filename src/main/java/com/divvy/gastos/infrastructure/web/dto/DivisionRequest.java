package com.divvy.gastos.infrastructure.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record DivisionRequest(
        @NotNull(message = "type is required") TipoDivisionDto type,
        @NotEmpty(message = "details must have at least one participant") Map<UUID, BigDecimal> details
) {
}
