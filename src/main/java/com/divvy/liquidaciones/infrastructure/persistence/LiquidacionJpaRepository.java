package com.divvy.liquidaciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LiquidacionJpaRepository extends JpaRepository<LiquidacionJpaEntity, UUID> {

    List<LiquidacionJpaEntity> findByGrupoIdOrderByFechaCalculoDesc(UUID grupoId);
}
