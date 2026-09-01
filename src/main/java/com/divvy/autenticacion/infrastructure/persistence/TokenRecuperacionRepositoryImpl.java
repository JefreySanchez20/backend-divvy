package com.divvy.autenticacion.infrastructure.persistence;

import com.divvy.autenticacion.domain.TokenRecuperacion;
import com.divvy.autenticacion.domain.TokenRecuperacionRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TokenRecuperacionRepositoryImpl implements TokenRecuperacionRepository {

    private final TokenRecuperacionJpaRepository jpaRepository;

    public TokenRecuperacionRepositoryImpl(TokenRecuperacionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TokenRecuperacion guardar(TokenRecuperacion token) {
        TokenRecuperacionJpaEntity entity = new TokenRecuperacionJpaEntity(
                token.id(),
                token.usuarioId(),
                token.token(),
                token.fechaExpiracion(),
                token.usado()
        );
        TokenRecuperacionJpaEntity guardado = jpaRepository.save(entity);
        return toDomain(guardado);
    }

    @Override
    public Optional<TokenRecuperacion> buscarPorToken(String token) {
        return jpaRepository.findByToken(token).map(TokenRecuperacionRepositoryImpl::toDomain);
    }

    private static TokenRecuperacion toDomain(TokenRecuperacionJpaEntity entity) {
        return TokenRecuperacion.reconstruir(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getToken(),
                entity.getFechaExpiracion(),
                entity.isUsado()
        );
    }
}
