package com.divvy.shared.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface RevokedTokenJpaRepository extends JpaRepository<RevokedTokenJpaEntity, String> {

    @Modifying
    @Query("DELETE FROM RevokedTokenJpaEntity t WHERE t.expiraEn < :ahora")
    void eliminarExpirados(Instant ahora);
}
