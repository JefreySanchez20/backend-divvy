package com.divvy.autenticacion.infrastructure.email;

import com.divvy.autenticacion.domain.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Manda el correo de recuperación por la API HTTP de SendGrid (puerto 443), no por SMTP:
 * Render bloquea las conexiones salientes a los puertos SMTP (25/465/587) en su plan
 * gratuito, así que un cliente SMTP normal se queda colgado hasta hacer timeout. Una API
 * por HTTPS no tiene ese problema.
 */
@Component
public class SendGridEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailSender.class);
    private static final String ENDPOINT = "https://api.sendgrid.com/v3/mail/send";
    private static final String NOMBRE_REMITENTE = "Divvy";

    private final RestClient restClient;
    private final String apiKey;
    private final String remitente;

    public SendGridEmailSender(
            RestClient.Builder restClientBuilder,
            @Value("${divvy.email.api-key}") String apiKey,
            @Value("${divvy.email.remitente}") String remitente
    ) {
        // Se recibe el Builder (no un RestClient ya armado) para poder enchufarle un
        // MockRestServiceServer en los tests sin golpear la red real.
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.remitente = remitente;
    }

    /**
     * Async a propósito: forgot-password debe responder rápido pase lo que pase con el
     * envío (la regla de negocio ya es "siempre 200"). Si SendGrid falla o tarda, que se
     * quede colgado este hilo en segundo plano, no la respuesta HTTP al usuario.
     */
    @Override
    @Async
    public void enviar(String destinatario, String asunto, String cuerpoHtml) {
        try {
            Map<String, Object> body = Map.of(
                    "personalizations", List.of(Map.of("to", List.of(Map.of("email", destinatario)))),
                    "from", Map.of("email", remitente, "name", NOMBRE_REMITENTE),
                    "subject", asunto,
                    "content", List.of(
                            Map.of("type", "text/plain", "value", aTextoPlano(cuerpoHtml)),
                            Map.of("type", "text/html", "value", cuerpoHtml)
                    )
            );

            // SendGrid responde 202 Accepted (sin cuerpo) cuando aceptó mandar el correo.
            restClient.post()
                    .uri(ENDPOINT)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Correo enviado a {} vía SendGrid", destinatario);
        } catch (RestClientException e) {
            log.error("No se pudo enviar el correo a través de SendGrid a {}", destinatario, e);
        }
    }

    /** Fallback simple de la parte de texto plano: no necesita ser perfecto, solo legible. */
    private static String aTextoPlano(String html) {
        return html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n\n")
                .replaceAll("<[^>]+>", "")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}
