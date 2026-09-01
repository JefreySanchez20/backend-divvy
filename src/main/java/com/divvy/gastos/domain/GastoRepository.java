package com.divvy.gastos.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GastoRepository {

    Gasto guardar(Gasto gasto);

    Optional<Gasto> buscarPorId(UUID id);

    List<Gasto> buscarPorGrupo(UUID grupoId);

    void eliminar(UUID id);
}
