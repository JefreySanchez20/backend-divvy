package com.divvy.grupos.infrastructure.web.dto;

import com.divvy.grupos.domain.EstadoGrupo;
import com.divvy.grupos.domain.Grupo;
import com.divvy.grupos.domain.Miembro;
import com.divvy.grupos.domain.Rol;

public final class GrupoDtoMapper {

    private GrupoDtoMapper() {
    }

    public static GrupoResponse toResponse(Grupo grupo) {
        return new GrupoResponse(
                grupo.id(),
                grupo.nombre(),
                grupo.fechaCreacion(),
                mapEstado(grupo.estado()),
                grupo.miembros().stream().map(GrupoDtoMapper::toResponse).toList()
        );
    }

    private static MiembroResponse toResponse(Miembro miembro) {
        return new MiembroResponse(miembro.usuarioId(), mapRol(miembro.rol()), miembro.fechaIngreso());
    }

    private static String mapEstado(EstadoGrupo estado) {
        return switch (estado) {
            case ACTIVO -> "ACTIVE";
            case ARCHIVADO -> "ARCHIVED";
        };
    }

    private static String mapRol(Rol rol) {
        return switch (rol) {
            case ADMIN -> "ADMIN";
            case MIEMBRO -> "MEMBER";
        };
    }
}
