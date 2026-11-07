-- Expand customer for sell/Girvi-style quick add (reference: first/last/city/mobile)
ALTER TABLE customer ADD COLUMN first_name VARCHAR(100) NULL;
ALTER TABLE customer ADD COLUMN last_name VARCHAR(100) NULL;
ALTER TABLE customer ADD COLUMN father_name VARCHAR(100) NULL;
ALTER TABLE customer ADD COLUMN city VARCHAR(100) NULL;
ALTER TABLE customer ADD COLUMN address VARCHAR(500) NULL;
ALTER TABLE customer ADD COLUMN party_type VARCHAR(32) NOT NULL DEFAULT 'CUSTOMER';

CREATE TABLE supplier (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    code            VARCHAR(64)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    phone           VARCHAR(32)     NULL,
    gstin           VARCHAR(20)     NULL,
    city            VARCHAR(100)    NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_supplier_code UNIQUE (code)
);

CREATE TABLE calc_setting (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    setting_key     VARCHAR(64)     NOT NULL,
    setting_value   VARCHAR(255)    NOT NULL,
    description     VARCHAR(255)    NULL,
    CONSTRAINT uk_calc_setting_key UNIQUE (setting_key)
);

INSERT INTO calc_setting (id, setting_key, setting_value, description) VALUES
('11111111-1111-1111-1111-111111111001', 'WASTAGE_MODE', 'ADD_TO_PURITY', 'ADD_TO_PURITY | ADD_WEIGHT'),
('11111111-1111-1111-1111-111111111002', 'MAKING_BASE', 'NET_WEIGHT', 'NET_WEIGHT | GROSS_WEIGHT | FINE_WEIGHT'),
('11111111-1111-1111-1111-111111111003', 'TAX_INCLUSIVE', 'false', 'Prices tax inclusive when true'),
('11111111-1111-1111-1111-111111111004', 'ROUND_OFF_SCALE', '2', 'Invoice money scale');

CREATE TABLE purchase_invoice (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    branch_id       VARCHAR(36)     NOT NULL,
    supplier_id     VARCHAR(36)     NULL,
    invoice_no      VARCHAR(64)     NOT NULL,
    invoice_date    DATE            NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'DRAFT',
    metal_amount    DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    making_amount   DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    stone_amount    DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    discount_amount DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    taxable_amount  DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    tax_amount      DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    round_off       DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    grand_total     DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    remarks         VARCHAR(500)    NULL,
    created_by      VARCHAR(36)     NULL,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_purchase_invoice_no UNIQUE (invoice_no),
    CONSTRAINT fk_purchase_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id)
);

CREATE TABLE purchase_invoice_line (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,
    purchase_invoice_id VARCHAR(36)     NOT NULL,
    line_no             INT             NOT NULL,
    stock_item_id       VARCHAR(36)     NULL,
    item_name           VARCHAR(255)    NOT NULL,
    metal_id            VARCHAR(36)     NULL,
    purity_id           VARCHAR(36)     NULL,
    quantity            DECIMAL(18, 4)  NOT NULL DEFAULT 1,
    gross_weight        DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    stone_weight        DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    net_weight          DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    purity_percent      DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    wastage_percent     DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    fine_weight         DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    final_fine_weight   DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    rate_per_gram       DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    metal_amount        DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    making_type         VARCHAR(32)     NOT NULL DEFAULT 'PER_GRAM',
    making_rate         DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    making_amount       DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    stone_amount        DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    line_total          DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    CONSTRAINT fk_pil_purchase FOREIGN KEY (purchase_invoice_id) REFERENCES purchase_invoice (id)
);

CREATE TABLE customer_order (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    branch_id       VARCHAR(36)     NOT NULL,
    customer_id     VARCHAR(36)     NULL,
    order_no        VARCHAR(64)     NOT NULL,
    order_date      DATE            NOT NULL,
    due_date        DATE            NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'OPEN',
    item_name       VARCHAR(255)    NOT NULL,
    metal_id        VARCHAR(36)     NULL,
    purity_id       VARCHAR(36)     NULL,
    target_weight   DECIMAL(18, 4)  NULL,
    estimated_amount DECIMAL(18, 2) NULL,
    advance_amount  DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    remarks         VARCHAR(500)    NULL,
    created_by      VARCHAR(36)     NULL,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uk_customer_order_no UNIQUE (order_no),
    CONSTRAINT fk_order_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE TABLE sales_invoice (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    branch_id       VARCHAR(36)     NOT NULL,
    customer_id     VARCHAR(36)     NULL,
    invoice_no      VARCHAR(64)     NOT NULL,
    invoice_date    DATE            NOT NULL,
    status          VARCHAR(32)     NOT NULL DEFAULT 'DRAFT',
    metal_amount    DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    making_amount   DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    stone_amount    DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    discount_amount DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    taxable_amount  DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    cgst_amount     DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    sgst_amount     DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    igst_amount     DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    tax_amount      DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    round_off       DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    grand_total     DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    paid_amount     DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    remarks         VARCHAR(500)    NULL,
    calc_snapshot   TEXT            NULL,
    created_by      VARCHAR(36)     NULL,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    version         BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_sales_invoice_no UNIQUE (invoice_no),
    CONSTRAINT fk_sales_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE TABLE sales_invoice_line (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,
    sales_invoice_id    VARCHAR(36)     NOT NULL,
    line_no             INT             NOT NULL,
    stock_item_id       VARCHAR(36)     NULL,
    item_name           VARCHAR(255)    NOT NULL,
    metal_id            VARCHAR(36)     NULL,
    purity_id           VARCHAR(36)     NULL,
    quantity            DECIMAL(18, 4)  NOT NULL DEFAULT 1,
    gross_weight        DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    stone_weight        DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    net_weight          DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    purity_percent      DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    wastage_percent     DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    fine_weight         DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    final_fine_weight   DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    rate_per_gram       DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    metal_amount        DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    making_type         VARCHAR(32)     NOT NULL DEFAULT 'PER_GRAM',
    making_rate         DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    making_amount       DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    stone_amount        DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    line_total          DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    CONSTRAINT fk_sil_sales FOREIGN KEY (sales_invoice_id) REFERENCES sales_invoice (id)
);

CREATE TABLE document_sequence (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    doc_type        VARCHAR(32)     NOT NULL,
    prefix          VARCHAR(32)     NOT NULL,
    next_value      BIGINT          NOT NULL DEFAULT 1,
    CONSTRAINT uk_document_sequence UNIQUE (doc_type)
);

INSERT INTO document_sequence (id, doc_type, prefix, next_value) VALUES
('22222222-2222-2222-2222-222222222001', 'SALE', 'SL', 1),
('22222222-2222-2222-2222-222222222002', 'PURCHASE', 'PU', 1),
('22222222-2222-2222-2222-222222222003', 'ORDER', 'OR', 1),
('22222222-2222-2222-2222-222222222004', 'CUSTOMER', 'C', 1),
('22222222-2222-2222-2222-222222222005', 'SUPPLIER', 'S', 1);
