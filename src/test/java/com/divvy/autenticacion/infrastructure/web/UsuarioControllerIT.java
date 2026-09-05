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
        "divvy.rate-limit.register=1000"
})
class UsuarioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String emailUnico() {
        return "user" + UUID.randomUUID() + "@demo.com";
    }

    private String loginYObtenerToken(String email, String password) throws Exception {
        String loginBody = objectMapper.writeValueAsString(Map.of("email", email, "password", password));
        String response = mockMvc.perform(post("/api/auth/login").contentType("application/json").content(loginBody))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("token").asString();
    }

    @Test
    void buscarPorEmail_usuarioExiste_devuelveDatosPublicos() throws Exception {
        String email = emailUnico();
        String registerBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234", "name", "Ana"));
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated());
        String token = loginYObtenerToken(email, "clave1234");

        mockMvc.perform(get("/api/users").param("email", email).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void buscarPorEmail_usuarioNoExiste_devuelve404() throws Exception {
        String email = emailUnico();
        String registerBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234", "name", "Ana"));
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated());
        String token = loginYObtenerToken(email, "clave1234");

        mockMvc.perform(get("/api/users").param("email", emailUnico()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void buscarPorEmail_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/users").param("email", "cualquiera@demo.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }
}
