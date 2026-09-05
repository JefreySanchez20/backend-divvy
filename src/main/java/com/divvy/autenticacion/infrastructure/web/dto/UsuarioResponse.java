package com.divvy.autenticacion.infrastructure.web.dto;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String email,
        String name
) {
}
