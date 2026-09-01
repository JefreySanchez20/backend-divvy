package com.divvy.autenticacion.domain;

import java.util.Optional;

public interface TokenRecuperacionRepository {

    TokenRecuperacion guardar(TokenRecuperacion token);

    Optional<TokenRecuperacion> buscarPorToken(String token);
}
