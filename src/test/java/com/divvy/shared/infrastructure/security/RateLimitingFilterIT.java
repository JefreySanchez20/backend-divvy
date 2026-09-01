package com.divvy.shared.infrastructure.security;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "divvy.rate-limit.window-minutes=15",
        "divvy.rate-limit.login=2",
        "divvy.rate-limit.register=1000",
        "divvy.rate-limit.forgot-password=1000",
        "divvy.rate-limit.reset-password=1000"
})
class RateLimitingFilterIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_excedeElLimite_devuelve429ConFormatoEstandar() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", "nadie" + UUID.randomUUID() + "@demo.com", "password", "clave1234"));

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login").contentType("application/json").content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"));
    }
}
