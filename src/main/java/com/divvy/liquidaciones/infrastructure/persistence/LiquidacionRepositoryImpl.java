package com.divvy.liquidaciones.infrastructure.persistence;

import com.divvy.liquidaciones.domain.Deuda;
import com.divvy.liquidaciones.domain.Liquidacion;
import com.divvy.liquidaciones.domain.LiquidacionRepository;
import com.divvy.shared.domain.Dinero;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LiquidacionRepositoryImpl implements LiquidacionRepository {

    private final LiquidacionJpaRepository jpaRepository;

    public LiquidacionRepositoryImpl(LiquidacionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Liquidacion guardar(Liquidacion liquidacion) {
        List<DeudaJpaEntity> deudas = liquidacion.deudas().stream()
                .map(d -> new DeudaJpaEntity(
                        d.id(), d.deudorId(), d.acreedorId(), d.monto().monto(), d.monto().moneda(), d.estado(), d.fechaPago()
                ))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));

        LiquidacionJpaEntity entity = new LiquidacionJpaEntity(
                liquidacion.id(), liquidacion.grupoId(), liquidacion.fechaCalculo(), deudas
        );
        LiquidacionJpaEntity guardado = jpaRepository.save(entity);
        return toDomain(guardado);
    }

    @Override
    public Optional<Liquidacion> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(LiquidacionRepositoryImpl::toDomain);
    }

    @Override
    public List<Liquidacion> buscarPorGrupo(UUID grupoId) {
        return jpaRepository.findByGrupoIdOrderByFechaCalculoDesc(grupoId).stream()
                .map(LiquidacionRepositoryImpl::toDomain)
                .toList();
    }

    private static Liquidacion toDomain(LiquidacionJpaEntity entity) {
        List<Deuda> deudas = entity.getDeudas().stream()
                .map(d -> Deuda.reconstruir(
                        d.getId(), d.getDeudorId(), d.getAcreedorId(),
                        Dinero.de(d.getMonto(), d.getMoneda()), d.getEstado(), d.getFechaPago()
                ))
                .toList();
        return Liquidacion.reconstruir(entity.getId(), entity.getGrupoId(), entity.getFechaCalculo(), deudas);
    }
}
