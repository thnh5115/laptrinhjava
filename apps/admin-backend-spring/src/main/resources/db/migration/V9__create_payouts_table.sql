-- V9: Create payouts table and sample data

CREATE TABLE payouts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'Owner who requested withdrawal',
    amount DECIMAL(10,2) NOT NULL COMMENT 'Withdrawal amount',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, COMPLETED',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When request was submitted',
    processed_at TIMESTAMP NULL COMMENT 'When request was processed',
    processed_by BIGINT COMMENT 'Admin who processed the request',
    notes TEXT COMMENT 'Admin notes or rejection reason',
    bank_account VARCHAR(255) COMMENT 'Bank account details',
    payment_method VARCHAR(50) COMMENT 'BANK_TRANSFER, PAYPAL, etc.',

    CONSTRAINT fk_payout_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_payout_processor FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_payout_user (user_id),
    INDEX idx_payout_status (status),
    INDEX idx_payout_requested_at (requested_at DESC),
    INDEX idx_payout_processed_at (processed_at DESC)
);

-- Sample payout requests from Owner (seller@test.local sold credits)
-- Payout 1: PENDING - Withdrawal request $302 (12.08 tons × $25/ton from sold credit)
INSERT INTO payouts (user_id, amount, status, bank_account, payment_method, requested_at)
SELECT
    u.id AS user_id,
    302.00 AS amount,
    'PENDING' AS status,
    '**** **** **** 1234' AS bank_account,
    'BANK_TRANSFER' AS payment_method,
    DATE_SUB(NOW(), INTERVAL 1 DAY) AS requested_at
FROM users u
WHERE u.email = 'buyer@test.local';

-- Payout 2: APPROVED - Withdrawal request $500 approved by admin
INSERT INTO payouts (user_id, amount, status, bank_account, payment_method, requested_at, processed_at, processed_by, notes)
SELECT
    u.id AS user_id,
    500.00 AS amount,
    'APPROVED' AS status,
    '**** **** **** 5678' AS bank_account,
    'BANK_TRANSFER' AS payment_method,
    DATE_SUB(NOW(), INTERVAL 5 DAY) AS requested_at,
    DATE_SUB(NOW(), INTERVAL 3 DAY) AS processed_at,
    (SELECT id FROM users WHERE email = 'admin@test.local') AS processed_by,
    'Approved for payment processing' AS notes
FROM users u
WHERE u.email = 'seller@test.local';

-- Payout 3: REJECTED - Withdrawal request $200 rejected due to insufficient balance
INSERT INTO payouts (user_id, amount, status, bank_account, payment_method, requested_at, processed_at, processed_by, notes)
SELECT
    u.id AS user_id,
    200.00 AS amount,
    'REJECTED' AS status,
    '**** **** **** 9012' AS bank_account,
    'PAYPAL' AS payment_method,
    DATE_SUB(NOW(), INTERVAL 7 DAY) AS requested_at,
    DATE_SUB(NOW(), INTERVAL 6 DAY) AS processed_at,
    (SELECT id FROM users WHERE email = 'admin@test.local') AS processed_by,
    'Rejected: Insufficient account balance' AS notes
FROM users u
WHERE u.email = 'buyer@test.local';

-- Payout 4: COMPLETED - Withdrawal request $750 completed successfully
INSERT INTO payouts (user_id, amount, status, bank_account, payment_method, requested_at, processed_at, processed_by, notes)
SELECT
    u.id AS user_id,
    750.00 AS amount,
    'COMPLETED' AS status,
    '**** **** **** 3456' AS bank_account,
    'BANK_TRANSFER' AS payment_method,
    DATE_SUB(NOW(), INTERVAL 10 DAY) AS requested_at,
    DATE_SUB(NOW(), INTERVAL 8 DAY) AS processed_at,
    (SELECT id FROM users WHERE email = 'admin@test.local') AS processed_by,
    'Payment completed and sent to bank account' AS notes
FROM users u
WHERE u.email = 'seller@test.local';

-- Verify sample data
-- Expected results:
-- 4 payouts total
-- 1 PENDING (from buyer@test.local, $302)
-- 1 APPROVED (from seller@test.local, $500)
-- 1 REJECTED (from buyer@test.local, $200)
-- 1 COMPLETED (from seller@test.local, $750)
