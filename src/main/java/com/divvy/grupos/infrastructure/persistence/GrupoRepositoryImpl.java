package com.divvy.grupos.infrastructure.persistence;

import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.GrupoRepository;
import com.divvy.grupos.domain.Miembro;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GrupoRepositoryImpl implements GrupoRepository {

    private final GrupoJpaRepository jpaRepository;

    public GrupoRepositoryImpl(GrupoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Grupo guardar(Grupo grupo) {
        GrupoJpaEntity entity = toEntity(grupo);
        GrupoJpaEntity guardado = jpaRepository.save(entity);
        return toDomain(guardado);
    }

    @Override
    public Optional<Grupo> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(GrupoRepositoryImpl::toDomain);
    }

    @Override
    public List<Grupo> buscarPorUsuario(UUID usuarioId) {
        return jpaRepository.buscarPorUsuario(usuarioId).stream()
                .map(GrupoRepositoryImpl::toDomain)
                .toList();
    }

    private static GrupoJpaEntity toEntity(Grupo grupo) {
        List<MiembroEmbeddable> miembros = grupo.miembros().stream()
                .map(m -> new MiembroEmbeddable(m.usuarioId(), m.rol(), m.fechaIngreso()))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        return new GrupoJpaEntity(
                grupo.id(),
                grupo.nombre(),
                grupo.fechaCreacion(),
                grupo.estado(),
                miembros
        );
    }

    private static Grupo toDomain(GrupoJpaEntity entity) {
        List<Miembro> miembros = entity.getMiembros().stream()
                .map(m -> new Miembro(m.getUsuarioId(), m.getRol(), m.getFechaIngreso()))
                .toList();

        return Grupo.reconstruir(
                entity.getId(),
                entity.getNombre(),
                entity.getFechaCreacion(),
                entity.getEstado(),
                miembros
        );
    }
}
