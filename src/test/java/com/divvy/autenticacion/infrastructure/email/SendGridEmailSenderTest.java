package com.divvy.autenticacion.infrastructure.email;

import com.divvy.autenticacion.domain.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

/**
 * Reproduce el bug real visto en producción con el envío anterior por Gmail SMTP: Render
 * bloquea los puertos SMTP salientes en su plan gratuito, así que la request de
 * forgot-password se colgaba hasta hacer timeout. SendGrid manda por HTTPS (443), que
 * nunca está bloqueado, y el envío sigue siendo async para no depender de qué tan rápido
 * responda el proveedor de correo.
 *
 * OJO: estos tests usan MockRestServiceServer — nunca pegan a la API real de SendGrid.
 * No repetir aquí el error anterior de probar contra el proveedor real con direcciones
 * de correo descartables (mailinator.com): eso fue justo lo que hizo que Brevo suspendiera
 * la cuenta por "actividad sospechosa".
 */
class SendGridEmailSenderTest {

    private static final String API_KEY = "clave-de-prueba";
    private static final String REMITENTE = "no-reply@divvy.app";
    private static final long RESPUESTA_LENTA_MS = 2000;

    @Configuration
    @EnableAsync
    static class Config {
        @Bean
        SendGridEmailSender sendGridEmailSender(RestClient.Builder restClientBuilder) {
            return new SendGridEmailSender(restClientBuilder, API_KEY, REMITENTE);
        }
    }

    private AnnotationConfigApplicationContext contexto(RestClient.Builder builder) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.registerBean(RestClient.Builder.class, () -> builder);
        ctx.register(Config.class);
        ctx.refresh();
        return ctx;
    }

    @Test
    void enviar_noBloqueaAlLlamadorAunqueSendGridSeaLento() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.sendgrid.com/v3/mail/send"))
                .andRespond(request -> {
                    try {
                        Thread.sleep(RESPUESTA_LENTA_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return withStatus(HttpStatus.ACCEPTED).createResponse(request);
                });

        try (AnnotationConfigApplicationContext ctx = contexto(builder)) {
            EmailSender emailSender = ctx.getBean(EmailSender.class);

            long inicio = System.currentTimeMillis();
            emailSender.enviar("ana@test.com", "Recupera tu contraseña", "<p>código</p>");
            long elapsedMs = System.currentTimeMillis() - inicio;

            assertThat(elapsedMs)
                    .as("enviar() debe retornar casi de inmediato: el envío real ocurre en otro hilo")
                    .isLessThan(RESPUESTA_LENTA_MS / 2);
        }
    }

    @Test
    void enviar_mandaElRequestCorrectoASendGrid() throws InterruptedException {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.sendgrid.com/v3/mail/send"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer " + API_KEY))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.from.email").value(REMITENTE))
                .andExpect(jsonPath("$.from.name").value("Divvy"))
                .andExpect(jsonPath("$.personalizations[0].to[0].email").value("ana@test.com"))
                .andExpect(jsonPath("$.subject").value("Recupera tu contraseña"))
                .andExpect(jsonPath("$.content[0].type").value("text/plain"))
                .andExpect(jsonPath("$.content[0].value").value("código 123456"))
                .andExpect(jsonPath("$.content[1].type").value("text/html"))
                .andExpect(jsonPath("$.content[1].value").value("<p>código <b>123456</b></p>"))
                .andRespond(withStatus(HttpStatus.ACCEPTED));

        try (AnnotationConfigApplicationContext ctx = contexto(builder)) {
            EmailSender emailSender = ctx.getBean(EmailSender.class);
            emailSender.enviar("ana@test.com", "Recupera tu contraseña", "<p>código <b>123456</b></p>");

            Thread.sleep(500); // el envío es async; hay que darle tiempo a que llegue al mock
            server.verify();
        }
    }

    @Test
    void enviar_siSendGridFalla_noPropagaLaExcepcion() throws InterruptedException {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.sendgrid.com/v3/mail/send"))
                .andRespond(withBadRequest());

        try (AnnotationConfigApplicationContext ctx = contexto(builder)) {
            EmailSender emailSender = ctx.getBean(EmailSender.class);

            assertThatCode(() -> emailSender.enviar("ana@test.com", "Asunto", "<p>cuerpo</p>"))
                    .as("un error de SendGrid se registra en el log, pero nunca debe romper al llamador "
                            + "(forgot-password siempre responde 200)")
                    .doesNotThrowAnyException();

            Thread.sleep(500);
            server.verify();
        }
    }
}
