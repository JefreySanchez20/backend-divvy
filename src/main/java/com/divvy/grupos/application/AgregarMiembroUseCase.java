package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;

import java.util.UUID;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

public class AgregarMiembroUseCase {

    private final GrupoRepository grupoRepository;

    public AgregarMiembroUseCase(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public Grupo ejecutar(UUID grupoId, UUID actorId, UUID usuarioId) {
        Grupo grupo = grupoRepository.buscarPorId(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + grupoId));
        // Igual que quitar miembros y archivar: sin esto cualquiera podría meterse en un grupo ajeno.
        if (!grupo.esAdmin(actorId)) {
            throw new UnauthorizedOperationException("Solo un miembro con rol ADMIN puede agregar miembros al grupo");
        }
        grupo.agregarMiembro(usuarioId);
        return grupoRepository.guardar(grupo);
    }
}
