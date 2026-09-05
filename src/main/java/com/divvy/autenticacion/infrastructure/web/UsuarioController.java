package com.divvy.autenticacion.infrastructure.web;

import com.divvy.autenticacion.application.BuscarUsuarioPorEmailUseCase;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.infrastructure.web.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Búsqueda de usuarios registrados")
@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    private final BuscarUsuarioPorEmailUseCase buscarUsuarioPorEmailUseCase;

    public UsuarioController(BuscarUsuarioPorEmailUseCase buscarUsuarioPorEmailUseCase) {
        this.buscarUsuarioPorEmailUseCase = buscarUsuarioPorEmailUseCase;
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
}
