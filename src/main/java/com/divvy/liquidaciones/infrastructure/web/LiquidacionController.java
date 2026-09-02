package com.divvy.liquidaciones.infrastructure.web;

import com.divvy.liquidaciones.application.CalcularLiquidacionUseCase;
import com.divvy.liquidaciones.application.MarcarDeudaComoPagadaUseCase;
import com.divvy.liquidaciones.application.ObtenerHistorialLiquidacionesUseCase;
import com.divvy.liquidaciones.infrastructure.web.dto.LiquidacionDtoMapper;
import com.divvy.liquidaciones.infrastructure.web.dto.LiquidacionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Settlements", description = "Cálculo y liquidación de deudas dentro de un grupo")
@RestController
public class LiquidacionController {

    private final CalcularLiquidacionUseCase calcularLiquidacionUseCase;
    private final ObtenerHistorialLiquidacionesUseCase obtenerHistorialLiquidacionesUseCase;
    private final MarcarDeudaComoPagadaUseCase marcarDeudaComoPagadaUseCase;

    public LiquidacionController(
            CalcularLiquidacionUseCase calcularLiquidacionUseCase,
            ObtenerHistorialLiquidacionesUseCase obtenerHistorialLiquidacionesUseCase,
            MarcarDeudaComoPagadaUseCase marcarDeudaComoPagadaUseCase
    ) {
        this.calcularLiquidacionUseCase = calcularLiquidacionUseCase;
        this.obtenerHistorialLiquidacionesUseCase = obtenerHistorialLiquidacionesUseCase;
        this.marcarDeudaComoPagadaUseCase = marcarDeudaComoPagadaUseCase;
    }

    @Operation(
            summary = "Calcular liquidación",
            description = "Calcula las deudas optimizadas actuales del grupo (mínimo de transacciones) y las registra en el historial."
    )
    @GetMapping("/api/groups/{groupId}/settlements")
    public LiquidacionResponse calcular(@AuthenticationPrincipal UUID actorId, @PathVariable UUID groupId) {
        return LiquidacionDtoMapper.toResponse(calcularLiquidacionUseCase.ejecutar(actorId, groupId));
    }

    @Operation(summary = "Historial de liquidaciones", description = "Devuelve las liquidaciones calculadas anteriormente para el grupo.")
    @GetMapping("/api/groups/{groupId}/settlements/history")
    public List<LiquidacionResponse> historial(@AuthenticationPrincipal UUID actorId, @PathVariable UUID groupId) {
        return obtenerHistorialLiquidacionesUseCase.ejecutar(actorId, groupId).stream()
                .map(LiquidacionDtoMapper::toResponse)
                .toList();
    }

    @Operation(
            summary = "Pagar deuda",
            description = "Marca una deuda como pagada. Solo el deudor o el acreedor de esa deuda pueden hacerlo."
    )
    @PostMapping("/api/settlements/{id}/debts/{debtId}/pay")
    public LiquidacionResponse pagar(@AuthenticationPrincipal UUID actorId, @PathVariable UUID id, @PathVariable UUID debtId) {
        return LiquidacionDtoMapper.toResponse(marcarDeudaComoPagadaUseCase.ejecutar(actorId, id, debtId));
    }
}
