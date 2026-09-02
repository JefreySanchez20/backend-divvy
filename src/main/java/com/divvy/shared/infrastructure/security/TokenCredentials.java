package com.divvy.shared.infrastructure.security;

import java.time.Instant;

public record TokenCredentials(String jti, Instant expiracion) {
}
