-- V7: Extended Girvi / Loan schema for multiple pledged items, payment modes, and storage details
ALTER TABLE girvi_loan ADD COLUMN packet_no VARCHAR(64) NULL;
ALTER TABLE girvi_loan ADD COLUMN box_no VARCHAR(64) NULL;
ALTER TABLE girvi_loan ADD COLUMN loan_type VARCHAR(32) NOT NULL DEFAULT 'SECURED';
ALTER TABLE girvi_loan ADD COLUMN first_month_interest BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE girvi_loan ADD COLUMN time_period_months INT NOT NULL DEFAULT 12;
ALTER TABLE girvi_loan ADD COLUMN dr_account VARCHAR(100) NULL DEFAULT 'Secured Loans';
ALTER TABLE girvi_loan ADD COLUMN item_location VARCHAR(100) NULL;
ALTER TABLE girvi_loan ADD COLUMN cash_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN cash_narration VARCHAR(255) NULL;
ALTER TABLE girvi_loan ADD COLUMN bank_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN bank_narration VARCHAR(255) NULL;
ALTER TABLE girvi_loan ADD COLUMN card_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN card_narration VARCHAR(255) NULL;
ALTER TABLE girvi_loan ADD COLUMN online_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN online_narration VARCHAR(255) NULL;
ALTER TABLE girvi_loan ADD COLUMN items_json TEXT NULL;

CREATE TABLE girvi_loan_item (
    id                  VARCHAR(36)     NOT NULL PRIMARY KEY,
    girvi_loan_id       VARCHAR(36)     NOT NULL,
    line_no             INT             NOT NULL,
    metal_type          VARCHAR(32)     NOT NULL DEFAULT 'GOLD',
    item_name           VARCHAR(255)    NOT NULL,
    quantity            INT             NOT NULL DEFAULT 1,
    gross_weight        DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    net_weight          DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    less_weight         DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    purity_percent      DECIMAL(10, 4)  NOT NULL DEFAULT 0,
    fine_weight         DECIMAL(18, 4)  NOT NULL DEFAULT 0,
    valuation_amount    DECIMAL(18, 2)  NOT NULL DEFAULT 0,
    photo_url           VARCHAR(255)    NULL,
    CONSTRAINT fk_gli_loan FOREIGN KEY (girvi_loan_id) REFERENCES girvi_loan (id)
);

