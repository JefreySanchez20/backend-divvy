package com.divvy.grupos.infrastructure.web;

import com.divvy.grupos.application.AgregarMiembroUseCase;
import com.divvy.grupos.application.ArchivarGrupoUseCase;
import com.divvy.grupos.application.CrearGrupoUseCase;
import com.divvy.grupos.application.ListarGruposDelUsuarioUseCase;
import com.divvy.grupos.application.ObtenerGrupoUseCase;
import com.divvy.grupos.application.RemoverMiembroUseCase;
import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.infrastructure.web.dto.AgregarMiembroRequest;
import com.divvy.grupos.infrastructure.web.dto.CrearGrupoRequest;
import com.divvy.grupos.infrastructure.web.dto.GrupoDtoMapper;
import com.divvy.grupos.infrastructure.web.dto.GrupoResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class GrupoController {

    private final CrearGrupoUseCase crearGrupoUseCase;
    private final ListarGruposDelUsuarioUseCase listarGruposDelUsuarioUseCase;
    private final ObtenerGrupoUseCase obtenerGrupoUseCase;
    private final AgregarMiembroUseCase agregarMiembroUseCase;
    private final RemoverMiembroUseCase removerMiembroUseCase;
    private final ArchivarGrupoUseCase archivarGrupoUseCase;

    public GrupoController(
            CrearGrupoUseCase crearGrupoUseCase,
            ListarGruposDelUsuarioUseCase listarGruposDelUsuarioUseCase,
            ObtenerGrupoUseCase obtenerGrupoUseCase,
            AgregarMiembroUseCase agregarMiembroUseCase,
            RemoverMiembroUseCase removerMiembroUseCase,
            ArchivarGrupoUseCase archivarGrupoUseCase
    ) {
        this.crearGrupoUseCase = crearGrupoUseCase;
        this.listarGruposDelUsuarioUseCase = listarGruposDelUsuarioUseCase;
        this.obtenerGrupoUseCase = obtenerGrupoUseCase;
        this.agregarMiembroUseCase = agregarMiembroUseCase;
        this.removerMiembroUseCase = removerMiembroUseCase;
        this.archivarGrupoUseCase = archivarGrupoUseCase;
    }

    @PostMapping
    public ResponseEntity<GrupoResponse> crear(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CrearGrupoRequest request
    ) {
        Grupo grupo = crearGrupoUseCase.ejecutar(request.name(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(GrupoDtoMapper.toResponse(grupo));
    }

    @GetMapping
    public List<GrupoResponse> listar(@RequestHeader("X-User-Id") UUID userId) {
        return listarGruposDelUsuarioUseCase.ejecutar(userId).stream()
                .map(GrupoDtoMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public GrupoResponse obtener(@PathVariable UUID id) {
        return GrupoDtoMapper.toResponse(obtenerGrupoUseCase.ejecutar(id));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<GrupoResponse> agregarMiembro(
            @PathVariable UUID id,
            @Valid @RequestBody AgregarMiembroRequest request
    ) {
        Grupo grupo = agregarMiembroUseCase.ejecutar(id, request.userId());
        return ResponseEntity.ok(GrupoDtoMapper.toResponse(grupo));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removerMiembro(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") UUID actorId
    ) {
        removerMiembroUseCase.ejecutar(id, actorId, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    public GrupoResponse archivar(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID actorId
    ) {
        return GrupoDtoMapper.toResponse(archivarGrupoUseCase.ejecutar(id, actorId));
    }
}
