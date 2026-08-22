package com.divvy.grupos.infrastructure.web;

import com.divvy.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class GrupoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID creadorId;
    private UUID invitadoId;

    @BeforeEach
    void setUp() {
        creadorId = crearUsuario();
        invitadoId = crearUsuario();
    }

    private UUID crearUsuario() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO usuarios (id, email, password_hash, nombre) VALUES (?, ?, ?, ?)",
                id, id + "@test.com", "hash", "Usuario de prueba"
        );
        return id;
    }

    @Test
    void flujoCompleto_crearListarAgregarRemoverArchivar() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Roomies"));
        String response = mockMvc.perform(post("/api/groups")
                        .header("X-User-Id", creadorId)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Roomies"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.members", hasSize(1)))
                .andExpect(jsonPath("$.members[0].role").value("ADMIN"))
                .andReturn().getResponse().getContentAsString();

        String grupoId = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/groups").header("X-User-Id", creadorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/groups/" + grupoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Roomies"));

        String agregarBody = objectMapper.writeValueAsString(Map.of("userId", invitadoId.toString()));
        mockMvc.perform(post("/api/groups/" + grupoId + "/members")
                        .contentType("application/json")
                        .content(agregarBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members", hasSize(2)));

        mockMvc.perform(delete("/api/groups/" + grupoId + "/members/" + creadorId)
                        .header("X-User-Id", invitadoId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED_OPERATION"));

        mockMvc.perform(delete("/api/groups/" + grupoId + "/members/" + invitadoId)
                        .header("X-User-Id", creadorId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));

        mockMvc.perform(patch("/api/groups/" + grupoId + "/archive")
                        .header("X-User-Id", creadorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    void crear_sinNombre_devuelve400ConFormatoDeErrorEstandar() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", ""));

        mockMvc.perform(post("/api/groups")
                        .header("X-User-Id", creadorId)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/groups"));
    }

    @Test
    void crear_sinHeaderXUserId_devuelve400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("name", "Roomies"));

        mockMvc.perform(post("/api/groups")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtener_grupoInexistente_devuelve404ConFormatoDeErrorEstandar() throws Exception {
        mockMvc.perform(get("/api/groups/" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ENTITY_NOT_FOUND"));
    }

    @Test
    void agregarMiembro_usuarioYaEsMiembro_devuelve400() throws Exception {
        String crearBody = objectMapper.writeValueAsString(Map.of("name", "Roomies"));
        String response = mockMvc.perform(post("/api/groups")
                        .header("X-User-Id", creadorId)
                        .contentType("application/json")
                        .content(crearBody))
                .andReturn().getResponse().getContentAsString();
        String grupoId = objectMapper.readTree(response).get("id").asString();

        String agregarBody = objectMapper.writeValueAsString(Map.of("userId", creadorId.toString()));
        mockMvc.perform(post("/api/groups/" + grupoId + "/members")
                        .contentType("application/json")
                        .content(agregarBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));
    }
}
