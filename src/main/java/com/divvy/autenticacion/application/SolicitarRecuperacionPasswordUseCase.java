package com.divvy.autenticacion.application;

import com.divvy.autenticacion.domain.EmailSender;
import com.divvy.autenticacion.domain.TokenRecuperacion;
import com.divvy.autenticacion.domain.TokenRecuperacionRepository;
import com.divvy.autenticacion.domain.Usuario;
import com.divvy.autenticacion.domain.UsuarioRepository;

import java.time.Duration;
import java.util.UUID;

public class SolicitarRecuperacionPasswordUseCase {

    private static final int MAX_INTENTOS_GENERACION = 5;

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final EmailSender emailSender;
    private final Duration validezToken;

    public SolicitarRecuperacionPasswordUseCase(
            UsuarioRepository usuarioRepository,
            TokenRecuperacionRepository tokenRecuperacionRepository,
            EmailSender emailSender,
            Duration validezToken
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRecuperacionRepository = tokenRecuperacionRepository;
        this.emailSender = emailSender;
        this.validezToken = validezToken;
    }

    public void ejecutar(String email) {
        usuarioRepository.buscarPorEmail(email).ifPresent(this::generarYEnviarToken);
    }

    private void generarYEnviarToken(Usuario usuario) {
        TokenRecuperacion token = generarTokenUnico(usuario.id());
        tokenRecuperacionRepository.guardar(token);

        String asunto = "Recupera tu contraseña en Divvy";
        String cuerpo = construirCuerpoHtml(token.token());
        emailSender.enviar(usuario.email(), asunto, cuerpo);
    }

    private TokenRecuperacion generarTokenUnico(UUID usuarioId) {
        for (int intento = 0; intento < MAX_INTENTOS_GENERACION; intento++) {
            TokenRecuperacion token = TokenRecuperacion.generar(UUID.randomUUID(), usuarioId, validezToken);
            if (tokenRecuperacionRepository.buscarPorToken(token.token()).isEmpty()) {
                return token;
            }
        }
        throw new IllegalStateException("No se pudo generar un código de recuperación único");
    }

    private String construirCuerpoHtml(String codigo) {
        return """
                <div style="font-family: -apple-system, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px 24px; color: #1a1a1a;">
                  <h1 style="font-size: 20px; margin: 0 0 24px 0; color: #4f46e5;">Divvy</h1>
                  <p style="font-size: 16px; line-height: 1.5; margin: 0 0 24px 0;">Usa este código para restablecer tu contraseña:</p>
                  <div style="background: #f4f4f7; border-radius: 8px; padding: 20px; text-align: center; margin: 0 0 24px 0;">
                    <span style="font-size: 32px; font-weight: 700; letter-spacing: 6px; color: #4f46e5; font-family: 'Courier New', monospace;">%s</span>
                  </div>
                  <p style="font-size: 14px; color: #666666; line-height: 1.5; margin: 0 0 8px 0;">Este código expira en %d minutos.</p>
                  <p style="font-size: 14px; color: #666666; line-height: 1.5; margin: 0;">Si no solicitaste esto, ignora este correo.</p>
                </div>
                """.formatted(codigo, validezToken.toMinutes());
    }
}
