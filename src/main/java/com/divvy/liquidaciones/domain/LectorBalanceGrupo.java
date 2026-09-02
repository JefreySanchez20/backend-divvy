package com.divvy.liquidaciones.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public interface LectorBalanceGrupo {

    /**
     * Devuelve el balance neto de cada usuario del grupo, agrupado por moneda.
     * Un valor positivo significa que al usuario le deben dinero; negativo, que él debe.
     */
    Map<String, Map<UUID, BigDecimal>> obtenerBalances(UUID grupoId);
}
