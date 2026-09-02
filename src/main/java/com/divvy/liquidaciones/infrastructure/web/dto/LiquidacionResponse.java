package com.divvy.liquidaciones.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LiquidacionResponse(
        UUID id,
        UUID groupId,
        Instant calculatedAt,
        List<DeudaResponse> debts
) {
}
