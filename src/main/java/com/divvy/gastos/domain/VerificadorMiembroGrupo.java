package com.divvy.gastos.domain;

import java.util.UUID;

public interface VerificadorMiembroGrupo {

    boolean esMiembroActivo(UUID grupoId, UUID usuarioId);
}
