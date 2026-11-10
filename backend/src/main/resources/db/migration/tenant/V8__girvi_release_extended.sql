-- V8: Extended Girvi Loan Release columns matching Omunim Core PHP logic
ALTER TABLE girvi_loan ADD COLUMN release_date DATE NULL;
ALTER TABLE girvi_loan ADD COLUMN release_ticket_no VARCHAR(64) NULL;
ALTER TABLE girvi_loan ADD COLUMN discount_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN extra_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN paid_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN paid_interest DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN monthly_interest_option VARCHAR(16) NOT NULL DEFAULT 'DD';
ALTER TABLE girvi_loan ADD COLUMN release_cash_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN release_bank_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN release_card_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN release_online_amount DECIMAL(18, 2) NOT NULL DEFAULT 0;
ALTER TABLE girvi_loan ADD COLUMN release_remarks VARCHAR(500) NULL;
ALTER TABLE girvi_loan ADD COLUMN locker_status VARCHAR(32) NOT NULL DEFAULT 'LOCKED';

