-- Add Stock: settings + rich stock entry (reference-aligned fields)

INSERT INTO calc_setting (id, setting_key, setting_value, description) VALUES
('11111111-1111-1111-1111-111111111010', 'stockValBy', 'byFFineWt', 'byGrossWt | byNetWt | byFineWt | byFFineWt'),
('11111111-1111-1111-1111-111111111011', 'stockMkgBy', 'mkgByNetWt', 'mkgByGrossWt | mkgByNetWt | mkgByFineWt | mkgByFFineWt'),
('11111111-1111-1111-1111-111111111012', 'stockCsWstgBy', 'custWastgByFineWt', 'custWastgByGrossWt | custWastgByNetWt | custWastgByFineWt'),
('11111111-1111-1111-1111-111111111013', 'stockLbrBy', 'lbByNetWt', 'lbByGrossWt | lbByNetWt | lbrByFineWt | lbrByFFineWt'),
('11111111-1111-1111-1111-111111111014', 'addStockFinalFinewt', 'byDefault', 'byDefault | byFineWt | byNetWt | byGrossWt'),
('11111111-1111-1111-1111-111111111015', 'stockFineBaseBy', 'byNetWt', 'byGrossWt | byNetWt — base wt for fine % calc');

ALTER TABLE stock_item ADD COLUMN category VARCHAR(128) NULL;
ALTER TABLE stock_item ADD COLUMN stock_type VARCHAR(32) NOT NULL DEFAULT 'RETAIL';
ALTER TABLE stock_item ADD COLUMN barcode_prefix VARCHAR(32) NULL;
ALTER TABLE stock_item ADD COLUMN barcode VARCHAR(64) NULL;
ALTER TABLE stock_item ADD COLUMN rfid_no VARCHAR(64) NULL;
ALTER TABLE stock_item ADD COLUMN hsn_no VARCHAR(32) NULL;
ALTER TABLE stock_item ADD COLUMN item_size VARCHAR(64) NULL;
ALTER TABLE stock_item ADD COLUMN other_info VARCHAR(500) NULL;

CREATE TABLE stock_entry (
    id                      VARCHAR(36)     NOT NULL PRIMARY KEY,
    branch_id               VARCHAR(36)     NOT NULL,
    stock_item_id           VARCHAR(36)     NOT NULL,
    entry_no                VARCHAR(64)     NOT NULL,
    entry_date              DATE            NOT NULL,
    stock_type              VARCHAR(32)     NOT NULL DEFAULT 'RETAIL',
    item_category           VARCHAR(128)    NULL,
    item_name               VARCHAR(255)    NOT NULL,
    metal_id                VARCHAR(36)     NULL,
    purity_id               VARCHAR(36)     NULL,
    barcode_prefix          VARCHAR(32)     NULL,
    barcode                 VARCHAR(64)     NULL,
    rfid_no                 VARCHAR(64)     NULL,
    hsn_no                  VARCHAR(32)     NULL,
    item_size               VARCHAR(64)     NULL,
    quantity                DECIMAL(18, 4)  NOT NULL DEFAULT 1,
    gross_weight            DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    pkt_weight              DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    less_weight             DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    tag_weight              DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    net_weight              DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    purity_percent          DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    wastage_percent         DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    final_purity_percent    DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    fine_weight             DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    final_fine_weight       DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    wastage_weight          DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    metal_rate              DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    lab_charges             DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    lab_charges_type        VARCHAR(16)     NOT NULL DEFAULT 'GM',
    total_lab_charges       DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    making_charges          DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    making_charges_type     VARCHAR(16)     NOT NULL DEFAULT 'GM',
    total_making_charges    DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    other_charges           DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    other_charges_type      VARCHAR(16)     NOT NULL DEFAULT 'FX',
    total_other_charges     DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    hallmark_charges        DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    hallmark_charges_type   VARCHAR(16)     NOT NULL DEFAULT 'FX',
    total_hallmark_charges  DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    stone_valuation         DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    metal_valuation         DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    tax_percent             DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    tax_amount              DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    final_valuation         DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    valuation_by            VARCHAR(32)     NULL,
    making_by               VARCHAR(32)     NULL,
    lab_by                  VARCHAR(32)     NULL,
    wastage_by              VARCHAR(32)     NULL,
    final_fine_by           VARCHAR(32)     NULL,
    fine_base_by            VARCHAR(32)     NULL,
    other_info              VARCHAR(500)    NULL,
    status                  VARCHAR(32)     NOT NULL DEFAULT 'POSTED',
    movement_id             VARCHAR(36)     NULL,
    created_by              VARCHAR(36)     NULL,
    created_at              TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at              TIMESTAMP(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    version                 BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_stock_entry_no UNIQUE (entry_no),
    CONSTRAINT fk_stock_entry_branch FOREIGN KEY (branch_id) REFERENCES branch (id),
    CONSTRAINT fk_stock_entry_item FOREIGN KEY (stock_item_id) REFERENCES stock_item (id)
);

CREATE INDEX idx_stock_entry_item ON stock_entry (stock_item_id, created_at);
CREATE INDEX idx_stock_entry_date ON stock_entry (entry_date);

CREATE TABLE stock_entry_packet (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    stock_entry_id  VARCHAR(36)     NOT NULL,
    line_no         INT             NOT NULL,
    description     VARCHAR(255)    NULL,
    quantity        DECIMAL(18, 4)  NOT NULL DEFAULT 1,
    weight          DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    operation       VARCHAR(8)      NOT NULL DEFAULT 'LESS',
    CONSTRAINT fk_sep_entry FOREIGN KEY (stock_entry_id) REFERENCES stock_entry (id)
);

CREATE TABLE stock_entry_crystal (
    id              VARCHAR(36)     NOT NULL PRIMARY KEY,
    stock_entry_id  VARCHAR(36)     NOT NULL,
    line_no         INT             NOT NULL,
    category        VARCHAR(128)    NULL,
    name            VARCHAR(255)    NOT NULL,
    clarity         VARCHAR(64)     NULL,
    color           VARCHAR(64)     NULL,
    shape           VARCHAR(64)     NULL,
    size_label      VARCHAR(64)     NULL,
    quantity        DECIMAL(18, 4)  NOT NULL DEFAULT 1,
    weight          DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    rate            DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    rate_type       VARCHAR(16)     NOT NULL DEFAULT 'PP',
    valuation       DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    auto_less       BOOLEAN         NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_sec_entry FOREIGN KEY (stock_entry_id) REFERENCES stock_entry (id)
);

INSERT INTO document_sequence (id, doc_type, prefix, next_value) VALUES
('22222222-2222-2222-2222-222222222006', 'STOCK', 'ST', 1);
