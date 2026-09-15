package com.divvy.autenticacion.infrastructure.web;

import com.divvy.autenticacion.application.BuscarUsuarioPorEmailUseCase;
import com.divvy.autenticacion.application.BuscarUsuariosPorIdsUseCase;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.infrastructure.web.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Users", description = "Búsqueda de usuarios registrados")
@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private final BuscarUsuarioPorEmailUseCase buscarUsuarioPorEmailUseCase;
    private final BuscarUsuariosPorIdsUseCase buscarUsuariosPorIdsUseCase;

    public UsuarioController(
            BuscarUsuarioPorEmailUseCase buscarUsuarioPorEmailUseCase,
            BuscarUsuariosPorIdsUseCase buscarUsuariosPorIdsUseCase
    ) {
        this.buscarUsuarioPorEmailUseCase = buscarUsuarioPorEmailUseCase;
        this.buscarUsuariosPorIdsUseCase = buscarUsuariosPorIdsUseCase;
    }

    @Operation(
            summary = "Buscar usuario por email",
            description = "Devuelve el usuario registrado con ese email exacto. Pensado para poder agregar miembros a un grupo a partir de su email."
    )
    @GetMapping
    public UsuarioResponse buscarPorEmail(@RequestParam String email) {
        Usuario usuario = buscarUsuarioPorEmailUseCase.ejecutar(email);
        return new UsuarioResponse(usuario.id(), usuario.email(), usuario.nombre());
    }

    @Operation(
            summary = "Buscar usuarios por id (batch)",
            description = "Devuelve los usuarios cuyo id está en la lista dada. Los ids que no correspondan a ningún usuario simplemente no aparecen en la respuesta. Pensado para resolver nombres/emails de los miembros de un grupo a partir de sus userId. Máximo " + BuscarUsuariosPorIdsUseCase.MAX_IDS_POR_CONSULTA + " ids por consulta."
    )
    @GetMapping("/batch")
    public List<UsuarioResponse> buscarPorIds(@RequestParam List<UUID> ids) {
        List<Usuario> usuarios = buscarUsuariosPorIdsUseCase.ejecutar(ids);
        return usuarios.stream()
                .map(usuario -> new UsuarioResponse(usuario.id(), usuario.email(), usuario.nombre()))
                .toList();
    }
}
