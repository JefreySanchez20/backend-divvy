package com.divvy.autenticacion.infrastructure.web;

import com.divvy.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String emailUnico() {
        return "user" + UUID.randomUUID() + "@demo.com";
    }

    @Test
    void register_datosValidos_creaUsuarioSinExponerPassword() throws Exception {
        String email = emailUnico();
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234", "name", "Ana"));

        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_emailDuplicado_devuelve400() throws Exception {
        String email = emailUnico();
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234", "name", "Ana"));

        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));
    }

    @Test
    void register_passwordCorta_devuelve400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", emailUnico(), "password", "corta", "name", "Ana"));

        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void login_credencialesCorrectas_devuelveTokenQueFunciona() throws Exception {
        String email = emailUnico();
        String registerBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234", "name", "Ana"));
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234"));
        String response = mockMvc.perform(post("/api/auth/login").contentType("application/json").content(loginBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("token").asString();

        mockMvc.perform(get("/api/groups").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void login_passwordIncorrecta_devuelve401() throws Exception {
        String email = emailUnico();
        String registerBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234", "name", "Ana"));
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "otraClave"));
        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void login_emailNoExiste_devuelve401() throws Exception {
        String loginBody = objectMapper.writeValueAsString(Map.of("email", emailUnico(), "password", "clave1234"));

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(loginBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void endpointProtegido_sinToken_devuelve401ConFormatoEstandar() throws Exception {
        mockMvc.perform(get("/api/groups"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }

    @Test
    void endpointProtegido_conTokenInvalido_devuelve401() throws Exception {
        mockMvc.perform(get("/api/groups").header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }

    @Test
    void logout_invalidaElTokenActual() throws Exception {
        String email = emailUnico();
        String registerBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234", "name", "Ana"));
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated());

        String loginBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234"));
        String response = mockMvc.perform(post("/api/auth/login").contentType("application/json").content(loginBody))
                .andReturn().getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asString();

        mockMvc.perform(get("/api/groups").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/groups").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }

    @Test
    void logout_sinToken_devuelve401() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }
}
