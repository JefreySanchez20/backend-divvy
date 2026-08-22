package com.divvy.grupos.infrastructure.web.dto;

import java.time.Instant;
import java.util.UUID;

public record MiembroResponse(
        UUID userId,
        String role,
        Instant joinedAt
) {
}
