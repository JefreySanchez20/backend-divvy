package com.divvy.grupos.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearGrupoRequest(
        @NotBlank(message = "name is required") String name
) {
}
