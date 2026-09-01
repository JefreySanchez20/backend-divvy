package com.divvy.gastos.infrastructure.persistence;

import com.divvy.gastos.domain.DivisionGasto;
import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.domain.GastoRepository;
import com.divvy.shared.domain.Dinero;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GastoRepositoryImpl implements GastoRepository {

    private final GastoJpaRepository jpaRepository;

    public GastoRepositoryImpl(GastoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Gasto guardar(Gasto gasto) {
        GastoJpaEntity entity = new GastoJpaEntity(
                gasto.id(),
                gasto.grupoId(),
                gasto.descripcion(),
                gasto.monto().monto(),
                gasto.monto().moneda(),
                gasto.pagadoPor(),
                gasto.fecha(),
                gasto.categoria(),
                gasto.division().tipo(),
                gasto.division().detalle()
        );
        GastoJpaEntity guardado = jpaRepository.save(entity);
        return toDomain(guardado);
    }

    @Override
    public Optional<Gasto> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(GastoRepositoryImpl::toDomain);
    }

    @Override
    public List<Gasto> buscarPorGrupo(UUID grupoId) {
        return jpaRepository.findByGrupoId(grupoId).stream()
                .map(GastoRepositoryImpl::toDomain)
                .toList();
    }

    @Override
    public void eliminar(UUID id) {
        jpaRepository.deleteById(id);
    }

    private static Gasto toDomain(GastoJpaEntity entity) {
        Dinero monto = Dinero.de(entity.getMonto(), entity.getMoneda());
        DivisionGasto division = DivisionGasto.reconstruir(entity.getTipoDivision(), entity.getDetalle());
        return Gasto.reconstruir(
                entity.getId(),
                entity.getGrupoId(),
                entity.getDescripcion(),
                monto,
                entity.getPagadoPor(),
                entity.getFecha(),
                entity.getCategoria(),
                division
        );
    }
}
