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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Groups", description = "Creación y administración de grupos y su membresía")
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

    @Operation(summary = "Crear grupo", description = "Crea un grupo nuevo. El usuario autenticado queda como único miembro, con rol ADMIN.")
    @PostMapping
    public ResponseEntity<GrupoResponse> crear(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CrearGrupoRequest request
    ) {
        Grupo grupo = crearGrupoUseCase.ejecutar(request.name(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(GrupoDtoMapper.toResponse(grupo));
    }

    @Operation(summary = "Listar mis grupos", description = "Devuelve los grupos donde el usuario autenticado es miembro.")
    @GetMapping
    public List<GrupoResponse> listar(@AuthenticationPrincipal UUID userId) {
        return listarGruposDelUsuarioUseCase.ejecutar(userId).stream()
                .map(GrupoDtoMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Obtener grupo", description = "Devuelve el detalle de un grupo por su id.")
    @GetMapping("/{id}")
    public GrupoResponse obtener(@PathVariable UUID id) {
        return GrupoDtoMapper.toResponse(obtenerGrupoUseCase.ejecutar(id));
    }

    @Operation(summary = "Agregar miembro", description = "Agrega un usuario al grupo con rol MEMBER.")
    @PostMapping("/{id}/members")
    public ResponseEntity<GrupoResponse> agregarMiembro(
            @PathVariable UUID id,
            @Valid @RequestBody AgregarMiembroRequest request
    ) {
        Grupo grupo = agregarMiembroUseCase.ejecutar(id, request.userId());
        return ResponseEntity.ok(GrupoDtoMapper.toResponse(grupo));
    }

    @Operation(summary = "Remover miembro", description = "Remueve a un miembro del grupo. Solo un ADMIN puede hacerlo, y el grupo debe quedar con al menos 2 miembros.")
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Void> removerMiembro(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UUID actorId
    ) {
        removerMiembroUseCase.ejecutar(id, actorId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Archivar grupo", description = "Archiva el grupo. Solo un ADMIN puede hacerlo.")
    @PatchMapping("/{id}/archive")
    public GrupoResponse archivar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID actorId
    ) {
        return GrupoDtoMapper.toResponse(archivarGrupoUseCase.ejecutar(id, actorId));
    }
}
