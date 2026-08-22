package com.divvy.grupos.domain;

import com.divvy.shared.domain.exception.EntityNotFoundException;
import com.divvy.shared.domain.exception.InvariantViolationException;
import com.divvy.shared.domain.exception.UnauthorizedOperationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Grupo {

    private static final int MINIMO_MIEMBROS = 2;

    private final UUID id;
    private final String nombre;
    private final Instant fechaCreacion;
    private EstadoGrupo estado;
    private final List<Miembro> miembros;

    private Grupo(UUID id, String nombre, Instant fechaCreacion, EstadoGrupo estado, List<Miembro> miembros) {
        this.id = id;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.miembros = new ArrayList<>(miembros);
    }

    public static Grupo crear(UUID id, String nombre, UUID creadorId) {
        Objects.requireNonNull(id, "El id del grupo no puede ser nulo");
        Objects.requireNonNull(creadorId, "El creadorId no puede ser nulo");
        validarNombre(nombre);

        Instant ahora = Instant.now();
        Miembro creador = new Miembro(creadorId, Rol.ADMIN, ahora);
        return new Grupo(id, nombre, ahora, EstadoGrupo.ACTIVO, List.of(creador));
    }

    public static Grupo reconstruir(UUID id, String nombre, Instant fechaCreacion, EstadoGrupo estado, List<Miembro> miembros) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(fechaCreacion);
        Objects.requireNonNull(estado);
        Objects.requireNonNull(miembros);
        return new Grupo(id, nombre, fechaCreacion, estado, miembros);
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new InvariantViolationException("El nombre del grupo no puede estar vacío");
        }
    }

    public void agregarMiembro(UUID usuarioId) {
        Objects.requireNonNull(usuarioId, "El usuarioId no puede ser nulo");
        if (estado == EstadoGrupo.ARCHIVADO) {
            throw new InvariantViolationException("No se pueden agregar miembros a un grupo archivado");
        }
        if (tieneMiembro(usuarioId)) {
            throw new InvariantViolationException("El usuario ya es miembro del grupo");
        }
        miembros.add(new Miembro(usuarioId, Rol.MIEMBRO, Instant.now()));
    }

    public void removerMiembro(UUID actorId, UUID usuarioId) {
        Objects.requireNonNull(actorId, "El actorId no puede ser nulo");
        Objects.requireNonNull(usuarioId, "El usuarioId no puede ser nulo");

        if (!esAdmin(actorId)) {
            throw new UnauthorizedOperationException("Solo un miembro con rol ADMIN puede remover miembros del grupo");
        }
        if (!tieneMiembro(usuarioId)) {
            throw new EntityNotFoundException("El usuario no es miembro de este grupo: " + usuarioId);
        }
        if (miembros.size() <= MINIMO_MIEMBROS) {
            throw new InvariantViolationException("Un grupo necesita un mínimo de " + MINIMO_MIEMBROS + " miembros activos para existir");
        }
        miembros.removeIf(m -> m.usuarioId().equals(usuarioId));
    }

    public void archivar(UUID actorId) {
        Objects.requireNonNull(actorId, "El actorId no puede ser nulo");
        if (!esAdmin(actorId)) {
            throw new UnauthorizedOperationException("Solo un miembro con rol ADMIN puede archivar el grupo");
        }
        this.estado = EstadoGrupo.ARCHIVADO;
    }

    public boolean esAdmin(UUID usuarioId) {
        return miembros.stream().anyMatch(m -> m.usuarioId().equals(usuarioId) && m.rol() == Rol.ADMIN);
    }

    public boolean tieneMiembro(UUID usuarioId) {
        return miembros.stream().anyMatch(m -> m.usuarioId().equals(usuarioId));
    }

    public UUID id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public Instant fechaCreacion() {
        return fechaCreacion;
    }

    public EstadoGrupo estado() {
        return estado;
    }

    public List<Miembro> miembros() {
        return Collections.unmodifiableList(miembros);
    }
}
