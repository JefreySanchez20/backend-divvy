package com.divvy.shared.domain;

import java.time.Instant;

public interface TokenBlacklist {

    void revocar(String jti, Instant expiracion);

    boolean estaRevocado(String jti);
}
