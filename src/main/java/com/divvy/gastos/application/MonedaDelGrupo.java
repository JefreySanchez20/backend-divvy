package com.divvy.gastos.application;

import com.divvy.shared.domain.ConsultorMonedaGrupo;
import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.InvariantViolationException;

import java.util.UUID;

/**
 * El balance suma montos por usuario sin convertir monedas, así que todos los gastos de un
 * grupo deben usar la moneda fija del grupo.
 */
final class MonedaDelGrupo {

    private MonedaDelGrupo() {
    }

    static void exigir(ConsultorMonedaGrupo consultor, UUID grupoId, String moneda) {
        String monedaDelGrupo = consultor.monedaDe(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado: " + grupoId));
        if (!monedaDelGrupo.equals(moneda)) {
            throw new InvariantViolationException(
                    "La moneda del gasto (" + moneda + ") debe ser la del grupo (" + monedaDelGrupo + ")");
        }
    }
}
