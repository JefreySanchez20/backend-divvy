package com.divvy.autenticacion.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "email is required") @Email(message = "email must be valid") String email,
        @NotBlank(message = "password is required") @Size(min = 8, message = "password must be at least 8 characters") String password,
        @NotBlank(message = "name is required") String name
) {
}
