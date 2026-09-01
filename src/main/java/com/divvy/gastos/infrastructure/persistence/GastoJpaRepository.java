package com.divvy.gastos.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GastoJpaRepository extends JpaRepository<GastoJpaEntity, UUID> {

    List<GastoJpaEntity> findByGrupoId(UUID grupoId);
}
