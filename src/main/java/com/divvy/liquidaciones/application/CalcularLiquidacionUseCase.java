package com.divvy.liquidaciones.application;

import com.divvy.liquidaciones.domain.CalculadoraDeudas;
import com.divvy.liquidaciones.domain.Deuda;
import com.divvy.liquidaciones.domain.LectorBalanceGrupo;
import com.divvy.liquidaciones.domain.Liquidacion;
import com.divvy.liquidaciones.domain.LiquidacionRepository;
import com.divvy.liquidaciones.domain.TransaccionSugerida;
import com.divvy.shared.domain.Dinero;
import com.divvy.shared.domain.VerificadorMiembroGrupo;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CalcularLiquidacionUseCase {

    private final LectorBalanceGrupo lectorBalanceGrupo;
    private final CalculadoraDeudas calculadoraDeudas;
    private final LiquidacionRepository liquidacionRepository;
    private final VerificadorMiembroGrupo verificadorMiembroGrupo;

    public CalcularLiquidacionUseCase(
            LectorBalanceGrupo lectorBalanceGrupo,
            CalculadoraDeudas calculadoraDeudas,
            LiquidacionRepository liquidacionRepository,
            VerificadorMiembroGrupo verificadorMiembroGrupo
    ) {
        this.lectorBalanceGrupo = lectorBalanceGrupo;
        this.calculadoraDeudas = calculadoraDeudas;
        this.liquidacionRepository = liquidacionRepository;
        this.verificadorMiembroGrupo = verificadorMiembroGrupo;
    }

    public Liquidacion ejecutar(UUID actorId, UUID grupoId) {
        if (!verificadorMiembroGrupo.esMiembroActivo(grupoId, actorId)) {
            throw new UnauthorizedOperationException("Debes ser miembro del grupo para consultar su liquidación");
        }

        Map<String, Map<UUID, BigDecimal>> balancesPorMoneda = lectorBalanceGrupo.obtenerBalances(grupoId);

        List<Deuda> deudas = new ArrayList<>();
        for (Map.Entry<String, Map<UUID, BigDecimal>> entry : balancesPorMoneda.entrySet()) {
            String moneda = entry.getKey();
            List<TransaccionSugerida> transacciones = calculadoraDeudas.calcular(entry.getValue());
            for (TransaccionSugerida transaccion : transacciones) {
                Dinero monto = Dinero.de(transaccion.monto(), moneda);
                deudas.add(Deuda.crear(UUID.randomUUID(), transaccion.deudorId(), transaccion.acreedorId(), monto));
            }
        }

        Liquidacion liquidacion = Liquidacion.calcular(UUID.randomUUID(), grupoId, deudas);
        return liquidacionRepository.guardar(liquidacion);
    }
}
