package com.divvy.autenticacion.application;

import com.divvy.shared.domain.TokenBlacklist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private TokenBlacklist tokenBlacklist;

    @Test
    void ejecutar_revocaElTokenEnLaListaNegra() {
        String jti = "token-123";
        Instant expiracion = Instant.now().plusSeconds(3600);

        LogoutUseCase useCase = new LogoutUseCase(tokenBlacklist);
        useCase.ejecutar(jti, expiracion);

        verify(tokenBlacklist).revocar(jti, expiracion);
    }
}
