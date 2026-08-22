package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;

import java.util.UUID;

public class ObtenerGrupoUseCase {

    private final GrupoRepository grupoRepository;

    public ObtenerGrupoUseCase(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public Grupo ejecutar(UUID grupoId) {
        return grupoRepository.buscarPorId(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + grupoId));
    }
}
