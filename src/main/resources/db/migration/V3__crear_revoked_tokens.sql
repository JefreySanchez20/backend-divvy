CREATE TABLE revoked_tokens (
    jti       VARCHAR(36) PRIMARY KEY,
    expira_en TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_revoked_tokens_expira ON revoked_tokens(expira_en);
