CREATE TABLE accounts (
    id               BIGINT       NOT NULL PRIMARY KEY,
    email            VARCHAR(255) NOT NULL,
    full_name        VARCHAR(120) NOT NULL,
    document         VARCHAR(14),
    phone            VARCHAR(16),
    keycloak_user_id VARCHAR(64),
    status           VARCHAR(20)  NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    created_by       BIGINT       NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    updated_by       BIGINT       NOT NULL,
    CONSTRAINT uk_accounts_email UNIQUE (email)
);

CREATE INDEX idx_accounts_keycloak_user_id ON accounts (keycloak_user_id);

CREATE TABLE password_recovery_tokens (
    id             BIGINT    NOT NULL PRIMARY KEY,
    account_id     BIGINT    NOT NULL,
    code_hash      VARCHAR(128) NOT NULL,
    expires_at     TIMESTAMP NOT NULL,
    used_at        TIMESTAMP,
    invalidated_at TIMESTAMP,
    created_at     TIMESTAMP NOT NULL,
    CONSTRAINT fk_recovery_account FOREIGN KEY (account_id) REFERENCES accounts (id)
);

CREATE INDEX idx_recovery_account_active ON password_recovery_tokens (account_id, used_at, invalidated_at);

CREATE TABLE authentication_throttles (
    operation         VARCHAR(40)  NOT NULL,
    subject_hash      VARCHAR(128) NOT NULL,
    window_started_at TIMESTAMP    NOT NULL,
    attempts          INTEGER      NOT NULL,
    PRIMARY KEY (operation, subject_hash)
);
