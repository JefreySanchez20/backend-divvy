package com.divvy.grupos.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GrupoRepository {

    Grupo guardar(Grupo grupo);

    Optional<Grupo> buscarPorId(UUID id);

    List<Grupo> buscarPorUsuario(UUID usuarioId);
}
