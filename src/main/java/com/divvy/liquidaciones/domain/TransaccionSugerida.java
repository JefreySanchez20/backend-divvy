package com.divvy.liquidaciones.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record TransaccionSugerida(UUID deudorId, UUID acreedorId, BigDecimal monto) {
}
