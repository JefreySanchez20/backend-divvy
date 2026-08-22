package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;

import java.util.UUID;

public class ArchivarGrupoUseCase {

    private final GrupoRepository grupoRepository;

    public ArchivarGrupoUseCase(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public Grupo ejecutar(UUID grupoId, UUID actorId) {
        Grupo grupo = grupoRepository.buscarPorId(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + grupoId));
        grupo.archivar(actorId);
        return grupoRepository.guardar(grupo);
    }
}
