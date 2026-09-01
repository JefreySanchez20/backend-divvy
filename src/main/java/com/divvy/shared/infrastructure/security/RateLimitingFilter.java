package com.divvy.shared.infrastructure.security;

import com.divvy.shared.infrastructure.exception.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Duration ventana;
    private final Map<String, Integer> limitesPorRuta;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(
            ObjectMapper objectMapper,
            @Value("${divvy.rate-limit.window-minutes}") long ventanaMinutos,
            @Value("${divvy.rate-limit.login}") int limiteLogin,
            @Value("${divvy.rate-limit.register}") int limiteRegister,
            @Value("${divvy.rate-limit.forgot-password}") int limiteForgotPassword,
            @Value("${divvy.rate-limit.reset-password}") int limiteResetPassword
    ) {
        this.objectMapper = objectMapper;
        this.ventana = Duration.ofMinutes(ventanaMinutos);
        this.limitesPorRuta = Map.of(
                "/api/auth/login", limiteLogin,
                "/api/auth/register", limiteRegister,
                "/api/auth/forgot-password", limiteForgotPassword,
                "/api/auth/reset-password", limiteResetPassword
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Integer limite = limitesPorRuta.get(request.getRequestURI());
        if (limite == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clave = request.getRequestURI() + ":" + request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(clave, k -> crearBucket(limite));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setContentType("application/json");
        ErrorResponse body = new ErrorResponse(
                Instant.now(), 429, "TOO_MANY_REQUESTS",
                "Demasiados intentos, intenta de nuevo más tarde", request.getRequestURI()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private Bucket crearBucket(int limite) {
        Bandwidth limitePorVentana = Bandwidth.builder()
                .capacity(limite)
                .refillIntervally(limite, ventana)
                .build();
        return Bucket.builder().addLimit(limitePorVentana).build();
    }
}
