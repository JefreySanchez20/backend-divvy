package com.divvy.liquidaciones.infrastructure.persistence;

import com.divvy.liquidaciones.domain.LectorBalanceGrupo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class LectorBalanceGrupoImpl implements LectorBalanceGrupo {

    private static final String QUERY = """
            SELECT usuario_id, moneda, SUM(monto) AS neto FROM (
                SELECT pagado_por AS usuario_id, moneda, monto FROM gastos WHERE grupo_id = ?
                UNION ALL
                SELECT d.usuario_id, g.moneda, -d.valor FROM gasto_division_detalle d
                JOIN gastos g ON g.id = d.gasto_id
                WHERE g.grupo_id = ?
            ) movimientos
            GROUP BY usuario_id, moneda
            HAVING SUM(monto) <> 0
            """;

    private final JdbcTemplate jdbcTemplate;

    public LectorBalanceGrupoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Map<UUID, BigDecimal>> obtenerBalances(UUID grupoId) {
        List<Map<String, Object>> filas = jdbcTemplate.queryForList(QUERY, grupoId, grupoId);

        Map<String, Map<UUID, BigDecimal>> resultado = new LinkedHashMap<>();
        for (Map<String, Object> fila : filas) {
            UUID usuarioId = (UUID) fila.get("usuario_id");
            String moneda = (String) fila.get("moneda");
            BigDecimal neto = (BigDecimal) fila.get("neto");
            resultado.computeIfAbsent(moneda, k -> new LinkedHashMap<>()).put(usuarioId, neto);
        }
        return resultado;
    }
}
