package com.divvy.gastos.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record DivisionResponse(
        String type,
        Map<UUID, BigDecimal> details
) {
}
