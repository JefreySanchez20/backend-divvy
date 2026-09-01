package com.divvy.autenticacion.infrastructure.web;

import com.divvy.TestcontainersConfiguration;
import com.divvy.autenticacion.infrastructure.email.FakeEmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "divvy.rate-limit.login=1000",
        "divvy.rate-limit.register=1000",
        "divvy.rate-limit.forgot-password=1000",
        "divvy.rate-limit.reset-password=1000"
})
class PasswordResetIT {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("([23456789ABCDEFGHJKMNPQRSTUVWXYZ]{6})");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FakeEmailSender fakeEmailSender;

    private String email;

    @BeforeEach
    void setUp() throws Exception {
        fakeEmailSender.limpiar();
        email = "user" + UUID.randomUUID() + "@demo.com";
        String registerBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "claveVieja1", "name", "Ana"));
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated());
    }

    private String extraerToken(String cuerpoCorreo) {
        Matcher matcher = TOKEN_PATTERN.matcher(cuerpoCorreo);
        assertThat(matcher.find()).as("el cuerpo del correo debe contener un token").isTrue();
        return matcher.group(1);
    }

    @Test
    void forgotPassword_emailExiste_devuelve200YEnviaCorreo() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email));

        mockMvc.perform(post("/api/auth/forgot-password").contentType("application/json").content(body))
                .andExpect(status().isOk());

        assertThat(fakeEmailSender.enviados()).hasSize(1);
        assertThat(fakeEmailSender.enviados().get(0).destinatario()).isEqualTo(email);
    }

    @Test
    void forgotPassword_emailNoExiste_devuelve200PeroNoEnviaCorreo() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", "nadie" + UUID.randomUUID() + "@demo.com"));

        mockMvc.perform(post("/api/auth/forgot-password").contentType("application/json").content(body))
                .andExpect(status().isOk());

        assertThat(fakeEmailSender.enviados()).isEmpty();
    }

    @Test
    void flujoCompleto_solicitarYRestablecer_cambiaLaPasswordDeVerdad() throws Exception {
        String forgotBody = objectMapper.writeValueAsString(Map.of("email", email));
        mockMvc.perform(post("/api/auth/forgot-password").contentType("application/json").content(forgotBody))
                .andExpect(status().isOk());

        String token = extraerToken(fakeEmailSender.enviados().get(0).cuerpo());

        String resetBody = objectMapper.writeValueAsString(Map.of("token", token, "newPassword", "claveNueva1"));
        mockMvc.perform(post("/api/auth/reset-password").contentType("application/json").content(resetBody))
                .andExpect(status().isOk());

        String loginConPasswordVieja = objectMapper.writeValueAsString(Map.of("email", email, "password", "claveVieja1"));
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(loginConPasswordVieja))
                .andExpect(status().isUnauthorized());

        String loginConPasswordNueva = objectMapper.writeValueAsString(Map.of("email", email, "password", "claveNueva1"));
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(loginConPasswordNueva))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void resetPassword_tokenNoExiste_devuelve400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("token", "token-que-no-existe", "newPassword", "claveNueva1"));

        mockMvc.perform(post("/api/auth/reset-password").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));
    }

    @Test
    void resetPassword_tokenYaUsado_fallaLaSegundaVez() throws Exception {
        String forgotBody = objectMapper.writeValueAsString(Map.of("email", email));
        mockMvc.perform(post("/api/auth/forgot-password").contentType("application/json").content(forgotBody))
                .andExpect(status().isOk());
        String token = extraerToken(fakeEmailSender.enviados().get(0).cuerpo());

        String resetBody = objectMapper.writeValueAsString(Map.of("token", token, "newPassword", "claveNueva1"));
        mockMvc.perform(post("/api/auth/reset-password").contentType("application/json").content(resetBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/reset-password").contentType("application/json").content(resetBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));
    }

    @Test
    void resetPassword_tokenExpirado_devuelve400() throws Exception {
        UUID usuarioId = jdbcTemplate.queryForObject("SELECT id FROM usuarios WHERE email = ?", UUID.class, email);
        String tokenExpirado = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO password_reset_tokens (id, usuario_id, token, fecha_expiracion, usado) VALUES (?, ?, ?, ?, false)",
                UUID.randomUUID(), usuarioId, tokenExpirado, java.sql.Timestamp.from(Instant.now().minusSeconds(60))
        );

        String body = objectMapper.writeValueAsString(Map.of("token", tokenExpirado, "newPassword", "claveNueva1"));
        mockMvc.perform(post("/api/auth/reset-password").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));
    }

    @Test
    void resetPassword_passwordMuyCorta_devuelve400Validation() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("token", "cualquiera", "newPassword", "corta"));

        mockMvc.perform(post("/api/auth/reset-password").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
