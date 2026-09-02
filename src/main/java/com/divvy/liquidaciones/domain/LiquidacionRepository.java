package com.divvy.liquidaciones.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LiquidacionRepository {

    Liquidacion guardar(Liquidacion liquidacion);

    Optional<Liquidacion> buscarPorId(UUID id);

    List<Liquidacion> buscarPorGrupo(UUID grupoId);
}
