package com.divvy.grupos.infrastructure.web.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GrupoResponse(
        UUID id,
        String name,
        Instant createdAt,
        String status,
        List<MiembroResponse> members
) {
}
