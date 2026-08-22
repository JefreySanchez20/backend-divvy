-- Usuarios (soporte para autenticación y referencias entre contextos)
CREATE TABLE usuarios (
    id             UUID PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    nombre         VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Bounded Context: Grupos
CREATE TABLE grupos (
    id             UUID PRIMARY KEY,
    nombre         VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now(),
    estado         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'ARCHIVADO'))
);

CREATE TABLE grupo_miembros (
    grupo_id       UUID NOT NULL REFERENCES grupos(id) ON DELETE CASCADE,
    usuario_id     UUID NOT NULL REFERENCES usuarios(id),
    rol            VARCHAR(20) NOT NULL CHECK (rol IN ('ADMIN', 'MIEMBRO')),
    fecha_ingreso  TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (grupo_id, usuario_id)
);

CREATE INDEX idx_grupo_miembros_usuario ON grupo_miembros(usuario_id);

-- Bounded Context: Gastos
CREATE TABLE gastos (
    id             UUID PRIMARY KEY,
    grupo_id       UUID NOT NULL REFERENCES grupos(id),
    descripcion    VARCHAR(500) NOT NULL,
    monto          NUMERIC(19, 4) NOT NULL CHECK (monto > 0),
    moneda         VARCHAR(3) NOT NULL,
    pagado_por     UUID NOT NULL REFERENCES usuarios(id),
    fecha          TIMESTAMPTZ NOT NULL,
    categoria      VARCHAR(100),
    tipo_division  VARCHAR(20) NOT NULL CHECK (tipo_division IN ('IGUAL', 'PORCENTAJE', 'MONTO_FIJO'))
);

CREATE INDEX idx_gastos_grupo ON gastos(grupo_id);
CREATE INDEX idx_gastos_pagado_por ON gastos(pagado_por);

-- Detalle de la división de cada gasto: mapa usuarioId -> monto/porcentaje
CREATE TABLE gasto_division_detalle (
    gasto_id       UUID NOT NULL REFERENCES gastos(id) ON DELETE CASCADE,
    usuario_id     UUID NOT NULL REFERENCES usuarios(id),
    valor          NUMERIC(19, 4) NOT NULL,
    PRIMARY KEY (gasto_id, usuario_id)
);

-- Bounded Context: Liquidaciones
CREATE TABLE liquidaciones (
    id             UUID PRIMARY KEY,
    grupo_id       UUID NOT NULL REFERENCES grupos(id),
    fecha_calculo  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_liquidaciones_grupo ON liquidaciones(grupo_id);

CREATE TABLE deudas (
    id               UUID PRIMARY KEY,
    liquidacion_id   UUID NOT NULL REFERENCES liquidaciones(id) ON DELETE CASCADE,
    deudor_id        UUID NOT NULL REFERENCES usuarios(id),
    acreedor_id      UUID NOT NULL REFERENCES usuarios(id),
    monto            NUMERIC(19, 4) NOT NULL CHECK (monto > 0),
    moneda           VARCHAR(3) NOT NULL,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'PAGADA')),
    fecha_pago       TIMESTAMPTZ
);

CREATE INDEX idx_deudas_liquidacion ON deudas(liquidacion_id);
CREATE INDEX idx_deudas_deudor ON deudas(deudor_id);
CREATE INDEX idx_deudas_acreedor ON deudas(acreedor_id);
