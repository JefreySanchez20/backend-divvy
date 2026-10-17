package com.divvy.autenticacion.infrastructure.email;

import com.divvy.autenticacion.domain.EmailSender;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reproduce el bug real que vieron en producción: forgot-password quedaba colgado
 * hasta que Gmail respondía (o el proxy de Render cortaba la conexión) porque el envío
 * de correo era síncrono y sin timeout. Usa un contexto Spring mínimo (sin base de
 * datos ni Testcontainers) solo para que @Async se aplique de verdad vía el proxy de
 * Spring — una llamada directa al método sin pasar por un bean gestionado no probaría
 * nada, porque la anotación no hace nada por sí sola.
 */
class GmailEmailSenderTest {

    private static final long ENVIO_LENTO_MS = 2000;

    @Configuration
    @EnableAsync
    static class Config {

        @Bean
        JavaMailSender mailSenderQueTardaEnResponder() throws Exception {
            JavaMailSender mailSender = Mockito.mock(JavaMailSender.class);
            Session session = Session.getDefaultInstance(new Properties());
            Mockito.when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(session));
            Mockito.doAnswer(invocation -> {
                Thread.sleep(ENVIO_LENTO_MS); // simula a Gmail tardando o colgándose
                return null;
            }).when(mailSender).send(Mockito.any(MimeMessage.class));
            return mailSender;
        }

        @Bean
        GmailEmailSender gmailEmailSender(JavaMailSender mailSender) {
            return new GmailEmailSender(mailSender);
        }
    }

    @Test
    void enviar_noBloqueaAlLlamadorAunqueElEnvioRealSeaLento() {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class)) {
            // @Async usa un proxy JDK que implementa EmailSender, no una subclase de
            // GmailEmailSender: hay que pedir el bean por la interfaz, como lo hace el resto
            // de la app (AutenticacionConfig inyecta EmailSender, nunca la clase concreta).
            EmailSender emailSender = ctx.getBean(EmailSender.class);

            long inicio = System.currentTimeMillis();
            emailSender.enviar("ana@test.com", "Recupera tu contraseña", "<p>código</p>");
            long elapsedMs = System.currentTimeMillis() - inicio;

            assertThat(elapsedMs)
                    .as("enviar() debe retornar casi de inmediato: el envío real ocurre en otro hilo, "
                            + "no debe bloquear al controlador que respondió el HTTP 200")
                    .isLessThan(ENVIO_LENTO_MS / 2);
        }
    }
}
