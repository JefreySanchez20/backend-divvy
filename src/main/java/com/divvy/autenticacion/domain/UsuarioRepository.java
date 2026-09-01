package com.divvy.autenticacion.domain;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(UUID id);
}
