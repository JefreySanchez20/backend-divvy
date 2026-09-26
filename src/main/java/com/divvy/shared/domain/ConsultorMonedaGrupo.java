package com.divvy.shared.domain;

import java.util.Optional;
import java.util.UUID;

public interface ConsultorMonedaGrupo {

    /** Moneda fija del grupo (ISO 4217), o vacío si el grupo no existe. */
    Optional<String> monedaDe(UUID grupoId);
}
