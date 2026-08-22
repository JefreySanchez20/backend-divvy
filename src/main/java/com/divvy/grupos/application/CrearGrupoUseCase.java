package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;

import java.util.UUID;

public class CrearGrupoUseCase {

    private final GrupoRepository grupoRepository;

    public CrearGrupoUseCase(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public Grupo ejecutar(String nombre, UUID creadorId) {
        Grupo grupo = Grupo.crear(UUID.randomUUID(), nombre, creadorId);
        return grupoRepository.guardar(grupo);
    }
}
