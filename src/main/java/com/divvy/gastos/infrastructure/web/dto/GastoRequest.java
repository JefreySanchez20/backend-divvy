package com.divvy.gastos.infrastructure.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GastoRequest(
        @NotBlank(message = "description is required") String description,
        @NotNull(message = "amount is required") @Positive(message = "amount must be greater than zero") BigDecimal amount,
        @NotBlank(message = "currency is required") String currency,
        @NotNull(message = "paidBy is required") UUID paidBy,
        @NotNull(message = "date is required") Instant date,
        String category,
        @NotNull(message = "division is required") @Valid DivisionRequest division
) {
}
