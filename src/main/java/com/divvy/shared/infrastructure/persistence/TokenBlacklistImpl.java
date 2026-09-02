package com.divvy.shared.infrastructure.persistence;

import com.divvy.shared.domain.TokenBlacklist;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class TokenBlacklistImpl implements TokenBlacklist {

    private final RevokedTokenJpaRepository jpaRepository;

    public TokenBlacklistImpl(RevokedTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void revocar(String jti, Instant expiracion) {
        jpaRepository.save(new RevokedTokenJpaEntity(jti, expiracion));
    }

    @Override
    public boolean estaRevocado(String jti) {
        return jpaRepository.existsById(jti);
    }
}
