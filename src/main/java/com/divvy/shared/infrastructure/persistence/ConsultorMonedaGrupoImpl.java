package com.divvy.shared.infrastructure.persistence;

import com.divvy.shared.domain.ConsultorMonedaGrupo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ConsultorMonedaGrupoImpl implements ConsultorMonedaGrupo {

    private static final String QUERY = "SELECT moneda FROM grupos WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;

    public ConsultorMonedaGrupoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<String> monedaDe(UUID grupoId) {
        return jdbcTemplate.queryForList(QUERY, String.class, grupoId).stream().findFirst();
    }
}
