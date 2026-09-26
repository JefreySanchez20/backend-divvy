package com.divvy.shared.infrastructure.persistence;

import com.divvy.shared.domain.VerificadorMiembroGrupo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class VerificadorMiembroGrupoImpl implements VerificadorMiembroGrupo {

    private static final String QUERY = """
            SELECT COUNT(*) FROM grupo_miembros gm
            JOIN grupos g ON g.id = gm.grupo_id
            WHERE gm.grupo_id = ? AND gm.usuario_id = ? AND g.estado = 'ACTIVO'
            """;

    private static final String QUERY_CUALQUIER_ESTADO = """
            SELECT COUNT(*) FROM grupo_miembros
            WHERE grupo_id = ? AND usuario_id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public VerificadorMiembroGrupoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean esMiembroActivo(UUID grupoId, UUID usuarioId) {
        Integer count = jdbcTemplate.queryForObject(QUERY, Integer.class, grupoId, usuarioId);
        return count != null && count > 0;
    }

    @Override
    public boolean esMiembro(UUID grupoId, UUID usuarioId) {
        Integer count = jdbcTemplate.queryForObject(QUERY_CUALQUIER_ESTADO, Integer.class, grupoId, usuarioId);
        return count != null && count > 0;
    }
}
