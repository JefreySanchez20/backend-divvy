package com.divvy.gastos.infrastructure.web;

import com.divvy.gastos.application.EditarGastoUseCase;
import com.divvy.gastos.application.EliminarGastoUseCase;
import com.divvy.gastos.application.ListarGastosPorGrupoUseCase;
import com.divvy.gastos.application.ObtenerGastoUseCase;
import com.divvy.gastos.application.RegistrarGastoUseCase;
import com.divvy.gastos.domain.Gasto;
import com.divvy.gastos.infrastructure.web.dto.GastoDtoMapper;
import com.divvy.gastos.infrastructure.web.dto.GastoRequest;
import com.divvy.gastos.infrastructure.web.dto.GastoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Expenses", description = "Registro y administración de gastos dentro de un grupo")
@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class GastoController {

    private final RegistrarGastoUseCase registrarGastoUseCase;
    private final ListarGastosPorGrupoUseCase listarGastosPorGrupoUseCase;
    private final ObtenerGastoUseCase obtenerGastoUseCase;
    private final EditarGastoUseCase editarGastoUseCase;
    private final EliminarGastoUseCase eliminarGastoUseCase;

    public GastoController(
            RegistrarGastoUseCase registrarGastoUseCase,
            ListarGastosPorGrupoUseCase listarGastosPorGrupoUseCase,
            ObtenerGastoUseCase obtenerGastoUseCase,
            EditarGastoUseCase editarGastoUseCase,
            EliminarGastoUseCase eliminarGastoUseCase
    ) {
        this.registrarGastoUseCase = registrarGastoUseCase;
        this.listarGastosPorGrupoUseCase = listarGastosPorGrupoUseCase;
        this.obtenerGastoUseCase = obtenerGastoUseCase;
        this.editarGastoUseCase = editarGastoUseCase;
        this.eliminarGastoUseCase = eliminarGastoUseCase;
    }

    @Operation(summary = "Registrar gasto", description = "Registra un gasto en el grupo y su división entre participantes.")
    @PostMapping
    public ResponseEntity<GastoResponse> registrar(
            @AuthenticationPrincipal UUID actorId,
            @PathVariable UUID groupId,
            @Valid @RequestBody GastoRequest request
    ) {
        Gasto gasto = registrarGastoUseCase.ejecutar(
                actorId, groupId, request.description(), request.amount(), request.currency(), request.paidBy(),
                request.date(), request.category(), GastoDtoMapper.mapTipoADominio(request.division().type()),
                request.division().details()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(GastoDtoMapper.toResponse(gasto));
    }

    @Operation(summary = "Listar gastos", description = "Devuelve los gastos registrados en el grupo.")
    @GetMapping
    public List<GastoResponse> listar(@AuthenticationPrincipal UUID actorId, @PathVariable UUID groupId) {
        return listarGastosPorGrupoUseCase.ejecutar(actorId, groupId).stream()
                .map(GastoDtoMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Obtener gasto", description = "Devuelve el detalle de un gasto por su id.")
    @GetMapping("/{id}")
    public GastoResponse obtener(@AuthenticationPrincipal UUID actorId, @PathVariable UUID groupId, @PathVariable UUID id) {
        return GastoDtoMapper.toResponse(obtenerGastoUseCase.ejecutar(actorId, groupId, id));
    }

    @Operation(summary = "Editar gasto", description = "Actualiza un gasto existente y su división.")
    @PutMapping("/{id}")
    public GastoResponse editar(
            @AuthenticationPrincipal UUID actorId,
            @PathVariable UUID groupId,
            @PathVariable UUID id,
            @Valid @RequestBody GastoRequest request
    ) {
        Gasto gasto = editarGastoUseCase.ejecutar(
                actorId, groupId, id, request.description(), request.amount(), request.currency(), request.paidBy(),
                request.date(), request.category(), GastoDtoMapper.mapTipoADominio(request.division().type()),
                request.division().details()
        );
        return GastoDtoMapper.toResponse(gasto);
    }

    @Operation(summary = "Eliminar gasto", description = "Elimina un gasto del grupo.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal UUID actorId, @PathVariable UUID groupId, @PathVariable UUID id) {
        eliminarGastoUseCase.ejecutar(actorId, groupId, id);
        return ResponseEntity.noContent().build();
    }
}
