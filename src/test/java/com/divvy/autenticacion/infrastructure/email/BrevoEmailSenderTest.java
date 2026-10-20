package com.divvy.autenticacion.infrastructure.email;

import com.divvy.autenticacion.domain.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Reproduce el bug real visto en producción con el envío anterior por Gmail SMTP: Render
 * bloquea los puertos SMTP salientes en su plan gratuito, así que la request de
 * forgot-password se colgaba hasta hacer timeout. Brevo manda por HTTPS (443), que nunca
 * está bloqueado, y el envío sigue siendo async para no depender de qué tan rápido
 * responda el proveedor de correo.
 */
class BrevoEmailSenderTest {

    private static final String API_KEY = "clave-de-prueba";
    private static final String REMITENTE = "no-reply@divvy.app";
    private static final long RESPUESTA_LENTA_MS = 2000;

    @Configuration
    @EnableAsync
    static class Config {
        @Bean
        BrevoEmailSender brevoEmailSender(RestClient.Builder restClientBuilder) {
            return new BrevoEmailSender(restClientBuilder, API_KEY, REMITENTE);
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
    void enviar_noBloqueaAlLlamadorAunqueBrevoSeaLento() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(request -> {
                    try {
                        Thread.sleep(RESPUESTA_LENTA_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return withSuccess("{}", MediaType.APPLICATION_JSON).createResponse(request);
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
    void enviar_mandaElRequestCorrectoABrevo() throws InterruptedException {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", API_KEY))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sender.email").value(REMITENTE))
                .andExpect(jsonPath("$.sender.name").value("Divvy"))
                .andExpect(jsonPath("$.to[0].email").value("ana@test.com"))
                .andExpect(jsonPath("$.subject").value("Recupera tu contraseña"))
                .andExpect(jsonPath("$.htmlContent").value("<p>código <b>123456</b></p>"))
                .andExpect(jsonPath("$.textContent").value("código 123456"))
                .andRespond(withSuccess("{\"messageId\":\"abc\"}", MediaType.APPLICATION_JSON));

        try (AnnotationConfigApplicationContext ctx = contexto(builder)) {
            EmailSender emailSender = ctx.getBean(EmailSender.class);
            emailSender.enviar("ana@test.com", "Recupera tu contraseña", "<p>código <b>123456</b></p>");

            Thread.sleep(500); // el envío es async; hay que darle tiempo a que llegue al mock
            server.verify();
        }
    }

    @Test
    void enviar_siBrevoFalla_noPropagaLaExcepcion() throws InterruptedException {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
                .andRespond(withBadRequest());

        try (AnnotationConfigApplicationContext ctx = contexto(builder)) {
            EmailSender emailSender = ctx.getBean(EmailSender.class);

            assertThatCode(() -> emailSender.enviar("ana@test.com", "Asunto", "<p>cuerpo</p>"))
                    .as("un error de Brevo se registra en el log, pero nunca debe romper al llamador "
                            + "(forgot-password siempre responde 200)")
                    .doesNotThrowAnyException();

            Thread.sleep(500);
            server.verify();
        }
    }
}
