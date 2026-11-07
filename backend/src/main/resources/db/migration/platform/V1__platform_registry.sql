CREATE TABLE tenant (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    code            VARCHAR(64)     NOT NULL,
    legal_name      VARCHAR(255)    NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    plan_code       VARCHAR(64)     NOT NULL DEFAULT 'STANDARD',
    region          VARCHAR(64)     NOT NULL DEFAULT 'IN',
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_tenant_code UNIQUE (code)
);

CREATE TABLE tenant_placement (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    tenant_id       VARCHAR(36)        NOT NULL,
    db_host         VARCHAR(255)    NOT NULL,
    db_port         INT             NOT NULL DEFAULT 3306,
    db_name         VARCHAR(128)    NOT NULL,
    db_user         VARCHAR(128)    NOT NULL,
    secret_ref      VARCHAR(255)    NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_tenant_placement_tenant UNIQUE (tenant_id),
    CONSTRAINT fk_tenant_placement_tenant FOREIGN KEY (tenant_id) REFERENCES tenant (id)
);

CREATE TABLE tenant_feature (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    tenant_id       VARCHAR(36)        NOT NULL,
    feature_code    VARCHAR(128)    NOT NULL,
    enabled         BOOLEAN         NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_tenant_feature UNIQUE (tenant_id, feature_code),
    CONSTRAINT fk_tenant_feature_tenant FOREIGN KEY (tenant_id) REFERENCES tenant (id)
);

CREATE TABLE provisioning_job (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    tenant_id       VARCHAR(36)        NOT NULL,
    job_type        VARCHAR(64)     NOT NULL,
    status          VARCHAR(32)     NOT NULL,
    detail_json     TEXT            NULL,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    finished_at     TIMESTAMP(3)    NULL,
    CONSTRAINT fk_provisioning_tenant FOREIGN KEY (tenant_id) REFERENCES tenant (id)
);

