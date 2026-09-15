package com.divvy.autenticacion.infrastructure.web;

import com.divvy.TestcontainersConfiguration;
import com.divvy.autenticacion.application.BuscarUsuariosPorIdsUseCase;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Test
    void buscarPorIds_idsExistenYNoExisten_devuelveSoloLosEncontrados() throws Exception {
        String emailAna = emailUnico();
        String registerBodyAna = objectMapper.writeValueAsString(Map.of("email", emailAna, "password", "clave1234", "name", "Ana"));
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBodyAna))
                .andExpect(status().isCreated());
        String tokenAna = loginYObtenerToken(emailAna, "clave1234");
        String idAna = objectMapper.readTree(
                        mockMvc.perform(get("/api/users").param("email", emailAna).header("Authorization", "Bearer " + tokenAna))
                                .andReturn().getResponse().getContentAsString())
                .get("id").asString();

        String idInexistente = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/users/batch").param("ids", idAna + "," + idInexistente).header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(idAna))
                .andExpect(jsonPath("$[0].email").value(emailAna))
                .andExpect(jsonPath("$[0].name").value("Ana"));
    }

    @Test
    void buscarPorIds_sinToken_devuelve401() throws Exception {
        mockMvc.perform(get("/api/users/batch").param("ids", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }

    @Test
    void buscarPorIds_masDeCienIds_devuelve400() throws Exception {
        String email = emailUnico();
        String registerBody = objectMapper.writeValueAsString(Map.of("email", email, "password", "clave1234", "name", "Ana"));
        mockMvc.perform(post("/api/auth/register").contentType("application/json").content(registerBody))
                .andExpect(status().isCreated());
        String token = loginYObtenerToken(email, "clave1234");

        String demasiadosIds = IntStream.range(0, BuscarUsuariosPorIdsUseCase.MAX_IDS_POR_CONSULTA + 1)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.joining(","));

        mockMvc.perform(get("/api/users/batch").param("ids", demasiadosIds).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));
    }
}
