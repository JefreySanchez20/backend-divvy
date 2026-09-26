package com.divvy.shared.domain;

import java.util.UUID;

public interface VerificadorMiembroGrupo {

    /**
     * Miembro de un grupo ACTIVO. Es la comprobación para ESCRIBIR (registrar, editar o eliminar
     * gastos): un grupo archivado ya no acepta cambios.
     */
    boolean esMiembroActivo(UUID grupoId, UUID usuarioId);

    /**
     * Miembro del grupo sin importar su estado. Es la comprobación para LEER: archivar un grupo
     * conserva todo su historial, así que sus miembros siguen pudiendo consultarlo.
     */
    boolean esMiembro(UUID grupoId, UUID usuarioId);
}
