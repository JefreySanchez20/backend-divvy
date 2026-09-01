package com.divvy.gastos.infrastructure.web;

import com.divvy.TestcontainersConfiguration;
import com.divvy.shared.infrastructure.security.JwtService;
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

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class GastoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    private UUID grupoId;
    private UUID usuario1;
    private UUID usuario2;
    private UUID usuarioAjeno;
    private String token1;
    private String token2;
    private String tokenAjeno;

    @BeforeEach
    void setUp() {
        usuario1 = crearUsuario();
        usuario2 = crearUsuario();
        usuarioAjeno = crearUsuario();
        token1 = jwtService.generar(usuario1);
        token2 = jwtService.generar(usuario2);
        tokenAjeno = jwtService.generar(usuarioAjeno);

        grupoId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO grupos (id, nombre, estado) VALUES (?, ?, 'ACTIVO')", grupoId, "Roomies");
        jdbcTemplate.update("INSERT INTO grupo_miembros (grupo_id, usuario_id, rol) VALUES (?, ?, 'ADMIN')", grupoId, usuario1);
        jdbcTemplate.update("INSERT INTO grupo_miembros (grupo_id, usuario_id, rol) VALUES (?, ?, 'MIEMBRO')", grupoId, usuario2);
    }

    private UUID crearUsuario() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO usuarios (id, email, password_hash, nombre) VALUES (?, ?, ?, ?)",
                id, id + "@test.com", "hash", "Usuario de prueba"
        );
        return id;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private Map<String, Object> gastoIgualBody(UUID pagadoPor, String monto) {
        return Map.of(
                "description", "Cena",
                "amount", monto,
                "currency", "PEN",
                "paidBy", pagadoPor.toString(),
                "date", "2026-08-31T20:00:00Z",
                "category", "Comida",
                "division", Map.of(
                        "type", "EQUAL",
                        "details", Map.of(usuario1.toString(), 0, usuario2.toString(), 0)
                )
        );
    }

    @Test
    void flujoCompleto_registrarListarObtenerEditarEliminar() throws Exception {
        String body = objectMapper.writeValueAsString(gastoIgualBody(usuario1, "100.00"));

        String response = mockMvc.perform(post("/api/groups/" + grupoId + "/expenses")
                        .header("Authorization", bearer(token1))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Cena"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.division.type").value("EQUAL"))
                .andReturn().getResponse().getContentAsString();

        String gastoId = objectMapper.readTree(response).get("id").asString();

        mockMvc.perform(get("/api/groups/" + grupoId + "/expenses").header("Authorization", bearer(token2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/groups/" + grupoId + "/expenses/" + gastoId).header("Authorization", bearer(token1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Comida"));

        String editBody = objectMapper.writeValueAsString(gastoIgualBody(usuario2, "80.00"));
        mockMvc.perform(put("/api/groups/" + grupoId + "/expenses/" + gastoId)
                        .header("Authorization", bearer(token2))
                        .contentType("application/json")
                        .content(editBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(80.00))
                .andExpect(jsonPath("$.paidBy").value(usuario2.toString()));

        mockMvc.perform(delete("/api/groups/" + grupoId + "/expenses/" + gastoId).header("Authorization", bearer(token1)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/groups/" + grupoId + "/expenses/" + gastoId).header("Authorization", bearer(token1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrar_montoNoCoincideConDivision_devuelve400() throws Exception {
        Map<String, Object> body = Map.of(
                "description", "Cena",
                "amount", "100.00",
                "currency", "PEN",
                "paidBy", usuario1.toString(),
                "date", "2026-08-31T20:00:00Z",
                "category", "Comida",
                "division", Map.of(
                        "type", "FIXED_AMOUNT",
                        "details", Map.of(usuario1.toString(), 30, usuario2.toString(), 30)
                )
        );

        mockMvc.perform(post("/api/groups/" + grupoId + "/expenses")
                        .header("Authorization", bearer(token1))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));
    }

    @Test
    void registrar_pagadoPorNoEsMiembro_devuelve400() throws Exception {
        String body = objectMapper.writeValueAsString(gastoIgualBody(usuarioAjeno, "100.00"));

        mockMvc.perform(post("/api/groups/" + grupoId + "/expenses")
                        .header("Authorization", bearer(token1))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));
    }

    @Test
    void registrar_actorNoEsMiembroDelGrupo_devuelve403() throws Exception {
        String body = objectMapper.writeValueAsString(gastoIgualBody(usuario1, "100.00"));

        mockMvc.perform(post("/api/groups/" + grupoId + "/expenses")
                        .header("Authorization", bearer(tokenAjeno))
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED_OPERATION"));
    }

    @Test
    void obtener_gastoDeOtroGrupo_devuelve404() throws Exception {
        String body = objectMapper.writeValueAsString(gastoIgualBody(usuario1, "100.00"));
        String response = mockMvc.perform(post("/api/groups/" + grupoId + "/expenses")
                        .header("Authorization", bearer(token1))
                        .contentType("application/json")
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        String gastoId = objectMapper.readTree(response).get("id").asString();

        UUID otroGrupoId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO grupos (id, nombre, estado) VALUES (?, ?, 'ACTIVO')", otroGrupoId, "Otro grupo");
        jdbcTemplate.update("INSERT INTO grupo_miembros (grupo_id, usuario_id, rol) VALUES (?, ?, 'ADMIN')", otroGrupoId, usuario1);

        mockMvc.perform(get("/api/groups/" + otroGrupoId + "/expenses/" + gastoId).header("Authorization", bearer(token1)))
                .andExpect(status().isNotFound());
    }
}
