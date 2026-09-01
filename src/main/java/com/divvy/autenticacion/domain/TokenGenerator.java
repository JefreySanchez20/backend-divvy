package com.divvy.autenticacion.domain;

import java.util.UUID;

public interface TokenGenerator {

    String generar(UUID usuarioId);
}
