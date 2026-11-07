-- Tenant foundation schema (offline single DB / per-tenant DB)

CREATE TABLE tenant_meta (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    code            VARCHAR(64)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    edition         VARCHAR(32)     NOT NULL DEFAULT 'OFFLINE',
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_tenant_meta_code UNIQUE (code)
);

CREATE TABLE app_user (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    username        VARCHAR(100)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    display_name    VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_app_user_username UNIQUE (username)
);

CREATE TABLE role (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    code            VARCHAR(64)     NOT NULL,
    name            VARCHAR(128)    NOT NULL,
    CONSTRAINT uk_role_code UNIQUE (code)
);

CREATE TABLE permission (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    code            VARCHAR(128)    NOT NULL,
    description     VARCHAR(255)    NULL,
    CONSTRAINT uk_permission_code UNIQUE (code)
);

CREATE TABLE user_role (
    user_id         VARCHAR(36)        NOT NULL,
    role_id         VARCHAR(36)        NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role (id)
);

CREATE TABLE role_permission (
    role_id         VARCHAR(36)        NOT NULL,
    permission_id   VARCHAR(36)        NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_perm_role FOREIGN KEY (role_id) REFERENCES role (id),
    CONSTRAINT fk_role_perm_perm FOREIGN KEY (permission_id) REFERENCES permission (id)
);

CREATE TABLE business_firm (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    code            VARCHAR(64)     NOT NULL,
    legal_name      VARCHAR(255)    NOT NULL,
    display_name    VARCHAR(255)    NOT NULL,
    gstin           VARCHAR(20)     NULL,
    pan             VARCHAR(20)     NULL,
    currency_code   VARCHAR(3)      NOT NULL DEFAULT 'INR',
    timezone        VARCHAR(64)     NOT NULL DEFAULT 'Asia/Kolkata',
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_business_firm_code UNIQUE (code)
);

CREATE TABLE branch (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    firm_id         VARCHAR(36)        NOT NULL,
    code            VARCHAR(64)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_branch_firm_code UNIQUE (firm_id, code),
    CONSTRAINT fk_branch_firm FOREIGN KEY (firm_id) REFERENCES business_firm (id)
);

CREATE TABLE user_branch_scope (
    user_id         VARCHAR(36)        NOT NULL,
    branch_id       VARCHAR(36)        NOT NULL,
    PRIMARY KEY (user_id, branch_id),
    CONSTRAINT fk_ubs_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_ubs_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE TABLE audit_event (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    actor_user_id   VARCHAR(36)        NULL,
    action          VARCHAR(128)    NOT NULL,
    entity_type     VARCHAR(128)    NOT NULL,
    entity_id       VARCHAR(64)     NULL,
    detail_json     TEXT            NULL,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX idx_audit_event_created ON audit_event (created_at);

