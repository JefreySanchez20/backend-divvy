package com.divvy.liquidaciones.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DeudaResponse(
        UUID id,
        UUID debtorId,
        UUID creditorId,
        BigDecimal amount,
        String currency,
        String status,
        Instant paidAt
) {
}
