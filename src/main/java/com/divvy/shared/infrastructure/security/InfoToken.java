package com.divvy.shared.infrastructure.security;

import java.time.Instant;
import java.util.UUID;

public record InfoToken(UUID usuarioId, String jti, Instant expiracion) {
}
