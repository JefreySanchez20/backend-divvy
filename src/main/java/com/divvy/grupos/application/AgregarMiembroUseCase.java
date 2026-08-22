package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import com.divvy.shared.domain.exception.EntityNotFoundException;

import java.util.UUID;

public class AgregarMiembroUseCase {

    private final GrupoRepository grupoRepository;

    public AgregarMiembroUseCase(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public Grupo ejecutar(UUID grupoId, UUID usuarioId) {
        Grupo grupo = grupoRepository.buscarPorId(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + grupoId));
        grupo.agregarMiembro(usuarioId);
        return grupoRepository.guardar(grupo);
    }
}
