package com.divvy.autenticacion.infrastructure.persistence;

import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryImpl(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity(
                usuario.id(),
                usuario.email(),
                usuario.passwordHash(),
                usuario.nombre(),
                usuario.fechaCreacion()
        );
        UsuarioJpaEntity guardado = jpaRepository.save(entity);
        return toDomain(guardado);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email).map(UsuarioRepositoryImpl::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(UsuarioRepositoryImpl::toDomain);
    }

    @Override
    public List<Usuario> buscarPorIds(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream()
                .map(UsuarioRepositoryImpl::toDomain)
                .toList();
    }

    private static Usuario toDomain(UsuarioJpaEntity entity) {
        return Usuario.reconstruir(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getNombre(),
                entity.getFechaCreacion()
        );
    }
}
