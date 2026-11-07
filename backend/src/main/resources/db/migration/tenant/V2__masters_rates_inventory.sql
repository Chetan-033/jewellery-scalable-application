CREATE TABLE customer (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    code            VARCHAR(64)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    phone           VARCHAR(32)     NULL,
    email           VARCHAR(255)    NULL,
    gstin           VARCHAR(20)     NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    created_by      VARCHAR(36)        NULL,
    updated_by      VARCHAR(36)        NULL,
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_customer_code UNIQUE (code)
);

CREATE TABLE metal (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    code            VARCHAR(32)     NOT NULL,
    name            VARCHAR(128)    NOT NULL,
    symbol          VARCHAR(16)     NULL,
    weight_unit     VARCHAR(16)     NOT NULL DEFAULT 'GRAM',
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_metal_code UNIQUE (code)
);

CREATE TABLE purity (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    metal_id        VARCHAR(36)        NOT NULL,
    label           VARCHAR(64)     NOT NULL,
    fineness        DECIMAL(10, 4)  NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_purity_metal FOREIGN KEY (metal_id) REFERENCES metal (id),
    CONSTRAINT uk_purity_metal_label UNIQUE (metal_id, label)
);

CREATE TABLE metal_rate (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    metal_id        VARCHAR(36)        NOT NULL,
    purity_id       VARCHAR(36)        NULL,
    branch_id       VARCHAR(36)        NULL,
    rate_per_gram   DECIMAL(18, 4)  NOT NULL,
    currency_code   VARCHAR(3)      NOT NULL DEFAULT 'INR',
    effective_from  TIMESTAMP(3)    NOT NULL,
    effective_to    TIMESTAMP(3)    NULL,
    source          VARCHAR(64)     NOT NULL DEFAULT 'MANUAL',
    created_by      VARCHAR(36)        NULL,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_metal_rate_metal FOREIGN KEY (metal_id) REFERENCES metal (id),
    CONSTRAINT fk_metal_rate_purity FOREIGN KEY (purity_id) REFERENCES purity (id),
    CONSTRAINT fk_metal_rate_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE INDEX idx_metal_rate_lookup ON metal_rate (metal_id, effective_from);

CREATE TABLE stock_item (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    sku             VARCHAR(64)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    metal_id        VARCHAR(36)        NULL,
    purity_id       VARCHAR(36)        NULL,
    stock_mode      VARCHAR(32)     NOT NULL DEFAULT 'QUANTITY_WEIGHT',
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_stock_item_sku UNIQUE (sku),
    CONSTRAINT fk_stock_item_metal FOREIGN KEY (metal_id) REFERENCES metal (id),
    CONSTRAINT fk_stock_item_purity FOREIGN KEY (purity_id) REFERENCES purity (id)
);

CREATE TABLE stock_balance (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    stock_item_id   VARCHAR(36)        NOT NULL,
    branch_id       VARCHAR(36)        NOT NULL,
    quantity        DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    gross_weight    DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    net_weight      DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    fine_weight     DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    version         BIGINT          NOT NULL DEFAULT 0,
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_stock_balance_item_branch UNIQUE (stock_item_id, branch_id),
    CONSTRAINT fk_stock_balance_item FOREIGN KEY (stock_item_id) REFERENCES stock_item (id),
    CONSTRAINT fk_stock_balance_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE TABLE stock_movement (
    id              VARCHAR(36)        NOT NULL PRIMARY KEY,
    stock_item_id   VARCHAR(36)        NOT NULL,
    branch_id       VARCHAR(36)        NOT NULL,
    movement_type   VARCHAR(32)     NOT NULL,
    quantity        DECIMAL(18, 4)  NOT NULL,
    gross_weight    DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    net_weight      DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    fine_weight     DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    source_type     VARCHAR(64)     NOT NULL,
    source_id       VARCHAR(64)     NULL,
    remarks         VARCHAR(500)    NULL,
    created_by      VARCHAR(36)        NULL,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_stock_movement_item FOREIGN KEY (stock_item_id) REFERENCES stock_item (id),
    CONSTRAINT fk_stock_movement_branch FOREIGN KEY (branch_id) REFERENCES branch (id)
);

CREATE INDEX idx_stock_movement_item ON stock_movement (stock_item_id, created_at);

