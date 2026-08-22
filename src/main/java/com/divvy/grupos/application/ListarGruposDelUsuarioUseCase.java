package com.divvy.grupos.application;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;

import java.util.List;
import java.util.UUID;

public class ListarGruposDelUsuarioUseCase {

    private final GrupoRepository grupoRepository;

    public ListarGruposDelUsuarioUseCase(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public List<Grupo> ejecutar(UUID usuarioId) {
        return grupoRepository.buscarPorUsuario(usuarioId);
    }
}
