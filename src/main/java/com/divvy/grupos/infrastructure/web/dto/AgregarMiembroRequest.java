package com.divvy.grupos.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AgregarMiembroRequest(
        @NotNull(message = "userId is required") UUID userId
) {
}
