package com.divvy.grupos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GrupoJpaRepository extends JpaRepository<GrupoJpaEntity, UUID> {

    @Query("SELECT g FROM GrupoJpaEntity g JOIN g.miembros m WHERE m.usuarioId = :usuarioId")
    List<GrupoJpaEntity> buscarPorUsuario(@Param("usuarioId") UUID usuarioId);
}
