package com.divvy.liquidaciones.infrastructure.web;

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
class LiquidacionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    private UUID grupoId;
    private UUID ana;
    private UUID beto;
    private UUID carla;
    private UUID ajeno;
    private String tokenAna;
    private String tokenBeto;
    private String tokenAjeno;

    @BeforeEach
    void setUp() throws Exception {
        ana = crearUsuario();
        beto = crearUsuario();
        carla = crearUsuario();
        ajeno = crearUsuario();
        tokenAna = jwtService.generar(ana);
        tokenBeto = jwtService.generar(beto);
        tokenAjeno = jwtService.generar(ajeno);

        grupoId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO grupos (id, nombre, estado) VALUES (?, ?, 'ACTIVO')", grupoId, "Viaje");
        jdbcTemplate.update("INSERT INTO grupo_miembros (grupo_id, usuario_id, rol) VALUES (?, ?, 'ADMIN')", grupoId, ana);
        jdbcTemplate.update("INSERT INTO grupo_miembros (grupo_id, usuario_id, rol) VALUES (?, ?, 'MIEMBRO')", grupoId, beto);
        jdbcTemplate.update("INSERT INTO grupo_miembros (grupo_id, usuario_id, rol) VALUES (?, ?, 'MIEMBRO')", grupoId, carla);

        String gastoBody = objectMapper.writeValueAsString(Map.of(
                "description", "Hotel",
                "amount", "90.00",
                "currency", "PEN",
                "paidBy", ana.toString(),
                "date", "2026-08-31T20:00:00Z",
                "category", "Alojamiento",
                "division", Map.of(
                        "type", "EQUAL",
                        "details", Map.of(ana.toString(), 0, beto.toString(), 0, carla.toString(), 0)
                )
        ));
        mockMvc.perform(post("/api/groups/" + grupoId + "/expenses")
                        .header("Authorization", "Bearer " + tokenAna)
                        .contentType("application/json")
                        .content(gastoBody))
                .andExpect(status().isCreated());
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
    void calcular_generaLasDosDeudasMinimasHaciaAna() throws Exception {
        mockMvc.perform(get("/api/groups/" + grupoId + "/settlements").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupId").value(grupoId.toString()))
                .andExpect(jsonPath("$.debts", hasSize(2)))
                .andExpect(jsonPath("$.debts[0].creditorId").value(ana.toString()))
                .andExpect(jsonPath("$.debts[0].amount").value(30.00))
                .andExpect(jsonPath("$.debts[0].status").value("PENDING"));
    }

    @Test
    void historial_incluyeLaLiquidacionCalculada() throws Exception {
        mockMvc.perform(get("/api/groups/" + grupoId + "/settlements").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/groups/" + grupoId + "/settlements/history").header("Authorization", "Bearer " + tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void pagar_deudorPagaSuDeuda_quedaMarcadaComoPagada() throws Exception {
        String response = mockMvc.perform(get("/api/groups/" + grupoId + "/settlements").header("Authorization", "Bearer " + tokenAna))
                .andReturn().getResponse().getContentAsString();

        var raiz = objectMapper.readTree(response);
        String liquidacionId = raiz.get("id").asString();
        String deudaBetoId = null;
        for (var deudaNode : raiz.get("debts")) {
            if (deudaNode.get("debtorId").asString().equals(beto.toString())) {
                deudaBetoId = deudaNode.get("id").asString();
            }
        }

        mockMvc.perform(post("/api/settlements/" + liquidacionId + "/debts/" + deudaBetoId + "/pay")
                        .header("Authorization", "Bearer " + tokenBeto))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.debts[?(@.id=='" + deudaBetoId + "')].status").value("PAID"));
    }

    @Test
    void pagar_deudaYaPagada_devuelve400() throws Exception {
        String response = mockMvc.perform(get("/api/groups/" + grupoId + "/settlements").header("Authorization", "Bearer " + tokenAna))
                .andReturn().getResponse().getContentAsString();
        var raiz = objectMapper.readTree(response);
        String liquidacionId = raiz.get("id").asString();
        String deudaBetoId = null;
        for (var deudaNode : raiz.get("debts")) {
            if (deudaNode.get("debtorId").asString().equals(beto.toString())) {
                deudaBetoId = deudaNode.get("id").asString();
            }
        }

        mockMvc.perform(post("/api/settlements/" + liquidacionId + "/debts/" + deudaBetoId + "/pay")
                        .header("Authorization", "Bearer " + tokenBeto))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/settlements/" + liquidacionId + "/debts/" + deudaBetoId + "/pay")
                        .header("Authorization", "Bearer " + tokenBeto))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVARIANT_VIOLATED"));
    }

    @Test
    void pagar_usuarioAjenoALaDeuda_devuelve403() throws Exception {
        String response = mockMvc.perform(get("/api/groups/" + grupoId + "/settlements").header("Authorization", "Bearer " + tokenAna))
                .andReturn().getResponse().getContentAsString();
        var raiz = objectMapper.readTree(response);
        String liquidacionId = raiz.get("id").asString();
        String deudaId = raiz.get("debts").get(0).get("id").asString();

        mockMvc.perform(post("/api/settlements/" + liquidacionId + "/debts/" + deudaId + "/pay")
                        .header("Authorization", "Bearer " + tokenAjeno))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED_OPERATION"));
    }

    @Test
    void calcular_usuarioNoMiembro_devuelve403() throws Exception {
        mockMvc.perform(get("/api/groups/" + grupoId + "/settlements").header("Authorization", "Bearer " + tokenAjeno))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED_OPERATION"));
    }
}
