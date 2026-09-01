package com.divvy.gastos.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GastoResponse(
        UUID id,
        String description,
        BigDecimal amount,
        String currency,
        UUID paidBy,
        Instant date,
        String category,
        DivisionResponse division
) {
}
