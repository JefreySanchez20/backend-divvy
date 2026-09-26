package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;

import java.util.UUID;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

public class ObtenerGrupoUseCase {

    private final GrupoRepository grupoRepository;

    public ObtenerGrupoUseCase(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public Grupo ejecutar(UUID grupoId, UUID actorId) {
        Grupo grupo = grupoRepository.buscarPorId(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + grupoId));
        // Un grupo archivado sigue siendo visible para sus miembros (historial); a los demás, nunca.
        if (!grupo.tieneMiembro(actorId)) {
            throw new UnauthorizedOperationException("Debes ser miembro del grupo para ver su detalle");
        }
        return grupo;
    }
}
