-- Industry commerce: customer KYC, party ledger, girvi, sell payments, stock summary helpers

ALTER TABLE customer ADD COLUMN title VARCHAR(16) NULL;
ALTER TABLE customer ADD COLUMN gender VARCHAR(8) NULL;
ALTER TABLE customer ADD COLUMN spouse_name VARCHAR(100) NULL;
ALTER TABLE customer ADD COLUMN alt_phone VARCHAR(32) NULL;
ALTER TABLE customer ADD COLUMN state_name VARCHAR(100) NULL;
ALTER TABLE customer ADD COLUMN village VARCHAR(100) NULL;
ALTER TABLE customer ADD COLUMN pincode VARCHAR(16) NULL;
ALTER TABLE customer ADD COLUMN pan_no VARCHAR(20) NULL;
ALTER TABLE customer ADD COLUMN aadhaar_no VARCHAR(20) NULL;
ALTER TABLE customer ADD COLUMN country_name VARCHAR(100) NULL DEFAULT 'India';

CREATE TABLE party_ledger_entry (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    branch_id       VARCHAR(36)     NOT NULL,
    customer_id     VARCHAR(36)     NOT NULL,
    entry_type      VARCHAR(32)     NOT NULL,
    ref_type        VARCHAR(32)     NULL,
    ref_id          VARCHAR(36)     NULL,
    ref_no          VARCHAR(64)     NULL,
    entry_date      DATE            NOT NULL,
    amount          DECIMAL(18, 2)  NOT NULL,
    balance_after   DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    remarks         VARCHAR(500)    NULL,
    created_by      VARCHAR(36)     NULL,
    created_at      TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT fk_ple_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_ple_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE INDEX idx_ple_customer ON party_ledger_entry (customer_id, entry_date);
CREATE INDEX idx_ple_type ON party_ledger_entry (entry_type, entry_date);

CREATE TABLE girvi_loan (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,
    branch_id           VARCHAR(36)     NOT NULL,
    customer_id         VARCHAR(36)     NOT NULL,
    ticket_no           VARCHAR(64)     NOT NULL,
    open_date           DATE            NOT NULL,
    status              VARCHAR(32)     NOT NULL DEFAULT 'ACTIVE',
    principal_amount    DECIMAL(18, 2)  NOT NULL,
    roi_percent         DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    interest_type       VARCHAR(32)     NOT NULL DEFAULT 'MONTHLY',
    metal_type          VARCHAR(32)     NULL,
    gross_weight        DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    net_weight          DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    fine_weight         DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    valuation_amount    DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    interest_amount     DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    total_amount        DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    item_desc           VARCHAR(500)    NULL,
    remarks             VARCHAR(500)    NULL,
    created_by          VARCHAR(36)     NULL,
    created_at          TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at          TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_girvi_ticket UNIQUE (ticket_no),
    CONSTRAINT fk_girvi_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_girvi_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE INDEX idx_girvi_customer ON girvi_loan (customer_id, status);

ALTER TABLE sales_invoice ADD COLUMN cash_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE sales_invoice ADD COLUMN card_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE sales_invoice ADD COLUMN online_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE sales_invoice ADD COLUMN cheque_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE sales_invoice ADD COLUMN udhaar_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE sales_invoice ADD COLUMN exchange_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE sales_invoice ADD COLUMN hallmark_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE sales_invoice ADD COLUMN other_charges DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE sales_invoice ADD COLUMN courier_charges DECIMAL(18, 2) NOT NULL DEFAULT 0;

ALTER TABLE sales_invoice_line ADD COLUMN barcode VARCHAR(64) NULL;
ALTER TABLE sales_invoice_line ADD COLUMN cust_wastage_percent DECIMAL(10, 4) NOT NULL DEFAULT 0;
ALTER TABLE sales_invoice_line ADD COLUMN value_added DECIMAL(18, 2) NOT NULL DEFAULT 0;

CREATE TABLE sale_exchange_line (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,
    sales_invoice_id    VARCHAR(36)     NOT NULL,
    line_no             INT             NOT NULL,
    metal_type          VARCHAR(32)     NOT NULL DEFAULT 'GOLD',
    description         VARCHAR(255)    NULL,
    gross_weight        DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    less_weight         DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    net_weight          DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    purity_percent      DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    fine_weight         DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    rate_per_gram       DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    valuation           DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    CONSTRAINT fk_sel_sale FOREIGN KEY (sales_invoice_id) REFERENCES sales_invoice (id)
);

INSERT INTO document_sequence (id, doc_type, prefix, next_value) VALUES
('22222222-2222-2222-2222-222222222007', 'GIRVI', 'B', 800),
('22222222-2222-2222-2222-222222222008', 'UDHAAR', 'UD', 1),
('22222222-2222-2222-2222-222222222009', 'ADVANCE', 'AD', 1);

INSERT INTO calc_setting (id, setting_key, setting_value, description) VALUES
('11111111-1111-1111-1111-111111111020', 'sellCustWstgMode', 'ADD_TO_NET_PCT', 'ADD_TO_NET_PCT | ADD_TO_PURITY'),
('11111111-1111-1111-1111-111111111021', 'defaultCgstPercent', '1.5', 'Default CGST % on sell'),
('11111111-1111-1111-1111-111111111022', 'defaultSgstPercent', '1.5', 'Default SGST % on sell');
