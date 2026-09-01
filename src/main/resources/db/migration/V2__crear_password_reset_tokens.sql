CREATE TABLE password_reset_tokens (
    id               UUID PRIMARY KEY,
    usuario_id       UUID NOT NULL REFERENCES usuarios(id),
    token            VARCHAR(255) NOT NULL UNIQUE,
    fecha_expiracion TIMESTAMPTZ NOT NULL,
    usado            BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_usuario ON password_reset_tokens(usuario_id);
