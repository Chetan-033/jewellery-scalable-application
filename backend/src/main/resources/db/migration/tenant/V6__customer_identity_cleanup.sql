-- Store blank identity fields as NULL so optional email, PAN, and Aadhaar
-- can be omitted by more than one customer.

UPDATE customer SET email = NULL WHERE email IS NOT NULL AND TRIM(email) = '';
UPDATE customer SET pan_no = NULL WHERE pan_no IS NOT NULL AND TRIM(pan_no) = '';
UPDATE customer SET aadhaar_no = NULL WHERE aadhaar_no IS NOT NULL AND TRIM(aadhaar_no) = '';
UPDATE customer SET phone = NULL WHERE phone IS NOT NULL AND TRIM(phone) = '';

UPDATE customer SET email = LOWER(TRIM(email)) WHERE email IS NOT NULL;
UPDATE customer SET pan_no = UPPER(REPLACE(pan_no, ' ', '')) WHERE pan_no IS NOT NULL;
UPDATE customer SET aadhaar_no = REPLACE(REPLACE(aadhaar_no, ' ', ''), '-', '') WHERE aadhaar_no IS NOT NULL;