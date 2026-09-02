package com.divvy.shared.domain;

import java.util.UUID;

public interface VerificadorMiembroGrupo {

    boolean esMiembroActivo(UUID grupoId, UUID usuarioId);
}
