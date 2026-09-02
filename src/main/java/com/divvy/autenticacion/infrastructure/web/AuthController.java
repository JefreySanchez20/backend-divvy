package com.divvy.autenticacion.infrastructure.web;

import com.divvy.autenticacion.application.LoginUseCase;
import com.divvy.autenticacion.application.LogoutUseCase;
import com.divvy.autenticacion.application.RegistrarUsuarioUseCase;
import com.divvy.autenticacion.application.RestablecerPasswordUseCase;
import com.divvy.autenticacion.application.SolicitarRecuperacionPasswordUseCase;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.infrastructure.web.dto.ForgotPasswordRequest;
import com.divvy.autenticacion.infrastructure.web.dto.LoginRequest;
import com.divvy.autenticacion.infrastructure.web.dto.LoginResponse;
import com.divvy.autenticacion.infrastructure.web.dto.RegisterRequest;
import com.divvy.autenticacion.infrastructure.web.dto.RegisterResponse;
import com.divvy.autenticacion.infrastructure.web.dto.ResetPasswordRequest;
import com.divvy.shared.infrastructure.security.TokenCredentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Registro y autenticación de usuarios")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final SolicitarRecuperacionPasswordUseCase solicitarRecuperacionPasswordUseCase;
    private final RestablecerPasswordUseCase restablecerPasswordUseCase;

    public AuthController(
            RegistrarUsuarioUseCase registrarUsuarioUseCase,
            LoginUseCase loginUseCase,
            LogoutUseCase logoutUseCase,
            SolicitarRecuperacionPasswordUseCase solicitarRecuperacionPasswordUseCase,
            RestablecerPasswordUseCase restablecerPasswordUseCase
    ) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.solicitarRecuperacionPasswordUseCase = solicitarRecuperacionPasswordUseCase;
        this.restablecerPasswordUseCase = restablecerPasswordUseCase;
    }

    @Operation(summary = "Registrar usuario", description = "Crea una cuenta nueva.")
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = registrarUsuarioUseCase.ejecutar(request.email(), request.password(), request.name());
        RegisterResponse response = new RegisterResponse(usuario.id(), usuario.email(), usuario.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login", description = "Autentica con email y contraseña, devuelve un JWT.")
    @SecurityRequirements
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String token = loginUseCase.ejecutar(request.email(), request.password());
        return new LoginResponse(token);
    }

    @Operation(summary = "Logout", description = "Invalida el token actual antes de que expire naturalmente.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        TokenCredentials credenciales = (TokenCredentials) authentication.getCredentials();
        logoutUseCase.ejecutar(credenciales.jti(), credenciales.expiracion());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Olvidé mi contraseña",
            description = "Envía un correo con un código de recuperación si el email existe. Responde 200 en ambos casos, exista o no la cuenta."
    )
    @SecurityRequirements
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        solicitarRecuperacionPasswordUseCase.ejecutar(request.email());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Restablecer contraseña", description = "Cambia la contraseña usando el código recibido por correo.")
    @SecurityRequirements
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        restablecerPasswordUseCase.ejecutar(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
