SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- DEMO SEED DATA FOR CARBON CREDIT MARKETPLACE
-- Assumptions:
--   - roles đã được seed từ script schema trước đó.
--   - Đây là seed chạy trên DB mới (trống dữ liệu business).
--   - Mật khẩu cho tất cả tài khoản demo: "password"
--     BCrypt hash: $2b$12$dNdGdF7tSpxk4RjSKQp5JucFD29Xd7k4HvmKwhPtj.TzGecWWyLwi
-- ============================================================================

-- ============================================================================
-- 1. USERS & ROLES
-- ============================================================================

-- Password hash for "password" (BCrypt)
SET @DEMO_PASSWORD_HASH = '$2b$12$dNdGdF7tSpxk4RjSKQp5JucFD29Xd7k4HvmKwhPtj.TzGecWWyLwi';

-- Admin
INSERT INTO users (id, email, password_hash, full_name, status, role_id, created_at, updated_at)
VALUES (
    1,
    'admin@gmail.com',
    @DEMO_PASSWORD_HASH,
    'System Administrator',
    'ACTIVE',
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    NOW(), NOW()
);

-- CVA / Auditor
INSERT INTO users (id, email, password_hash, full_name, status, role_id, created_at, updated_at)
VALUES (
    2,
    'cva@example.com',
    @DEMO_PASSWORD_HASH,
    'CVA Auditor',
    'ACTIVE',
    (SELECT id FROM roles WHERE name = 'AUDITOR'),
    NOW(), NOW()
);

-- Buyer accounts
INSERT INTO users (id, email, password_hash, full_name, status, role_id, created_at, updated_at)
VALUES
(
    3,
    'buyer@example.com',
    @DEMO_PASSWORD_HASH,
    'Primary Buyer',
    'ACTIVE',
    (SELECT id FROM roles WHERE name = 'BUYER'),
    NOW(), NOW()
),
(
    4,
    'buyer2@example.com',
    @DEMO_PASSWORD_HASH,
    'Secondary Buyer',
    'ACTIVE',
    (SELECT id FROM roles WHERE name = 'BUYER'),
    NOW(), NOW()
);

-- EV Owner accounts
INSERT INTO users (id, email, password_hash, full_name, status, role_id, created_at, updated_at)
VALUES
(
    5,
    'owner@example.com',
    @DEMO_PASSWORD_HASH,
    'EV Owner One',
    'ACTIVE',
    (SELECT id FROM roles WHERE name = 'EV_OWNER'),
    NOW(), NOW()
),
(
    6,
    'owner2@example.com',
    @DEMO_PASSWORD_HASH,
    'EV Owner Two',
    'ACTIVE',
    (SELECT id FROM roles WHERE name = 'EV_OWNER'),
    NOW(), NOW()
);

-- ============================================================================
-- 2. E-WALLETS, DEPOSITS, PAYOUTS
-- ============================================================================

-- E-wallets for main users
INSERT INTO e_wallets (id, user_id, balance, currency, status, updated_at)
VALUES
    (1, 1,  500.00, 'USD', 'ACTIVE', NOW()),   -- Admin (for payouts, demo)
    (2, 3, 2000.00, 'USD', 'ACTIVE', NOW()),   -- Buyer 1
    (3, 4, 1500.00, 'USD', 'ACTIVE', NOW()),   -- Buyer 2
    (4, 5,  300.00, 'USD', 'ACTIVE', NOW()),   -- EV Owner 1
    (5, 6,  250.00, 'USD', 'ACTIVE', NOW());   -- EV Owner 2

-- Deposits for Buyers
INSERT INTO deposits (id, user_id, e_wallet_id, amount, status, payment_gateway_ref, created_at)
VALUES
    (1, 3, 2, 1000.00, 'COMPLETED', 'GATEWAY-DEP-0001', NOW() - INTERVAL 3 DAY),
    (2, 4, 3,  800.00, 'COMPLETED', 'GATEWAY-DEP-0002', NOW() - INTERVAL 2 DAY);

-- Payout request for EV Owner 1
INSERT INTO payouts (id, user_id, e_wallet_id, amount, status, requested_at, processed_at, processed_by, notes, bank_account, payment_method)
VALUES
(
    1,
    5,
    4,
    120.00,
    'COMPLETED',
    NOW() - INTERVAL 5 DAY,
    NOW() - INTERVAL 4 DAY,
    1, -- processed by admin
    'First payout for EV Owner 1',
    'Bank XYZ - ****1234',
    'BANK_TRANSFER'
);

-- ============================================================================
-- 3. JOURNEYS & VERIFICATION
-- ============================================================================

INSERT INTO journeys (id, user_id, journey_date, start_location, end_location, distance_km,
                      energy_used_kwh, status, verified_by, verified_at, rejection_reason,
                      credits_generated, created_at)
VALUES
-- Pending journey (not yet verified)
(
    1,
    5,                         -- owner@example.com
    CURDATE() - INTERVAL 3 DAY,
    'District 1, HCMC',
    'Thu Duc City, HCMC',
    25.50,
    5.20,
    'PENDING',
    NULL,
    NULL,
    NULL,
    NULL,
    NOW() - INTERVAL 3 DAY
),
-- Approved journey for Owner 1
(
    2,
    5,
    CURDATE() - INTERVAL 2 DAY,
    'Hanoi Center',
    'Noi Bai Airport',
    30.00,
    6.00,
    'APPROVED',
    2,                         -- verified by cva@example.com
    NOW() - INTERVAL 2 DAY,
    NULL,
    15.00,
    NOW() - INTERVAL 2 DAY
),
-- Approved journey for Owner 2
(
    3,
    6,
    CURDATE() - INTERVAL 1 DAY,
    'Da Nang Center',
    'Hoi An',
    40.00,
    7.50,
    'APPROVED',
    2,
    NOW() - INTERVAL 1 DAY,
    NULL,
    20.00,
    NOW() - INTERVAL 1 DAY
);

-- ============================================================================
-- 4. CARBON CREDITS
-- ============================================================================

INSERT INTO carbon_credits (id, owner_id, journey_id, amount, status, price_per_credit,
                            listed_at, sold_at, buyer_id, created_at)
VALUES
-- Credits still available for Owner 1
(
    1,
    5,          -- owner@example.com
    2,          -- approved journey
    15.00,
    'AVAILABLE',
    10.00,      -- 10 USD per credit
    NOW() - INTERVAL 2 DAY,
    NULL,
    NULL,
    NOW() - INTERVAL 2 DAY
),
-- Credits already sold from Owner 2 to Buyer 1
(
    2,
    6,          -- owner2@example.com
    3,
    20.00,
    'SOLD',
    12.00,
    NOW() - INTERVAL 2 DAY,
    NOW() - INTERVAL 1 DAY,
    3,          -- buyer@example.com
    NOW() - INTERVAL 2 DAY
);

-- ============================================================================
-- 5. LISTINGS, AUCTIONS, BIDS
-- ============================================================================

-- Listings
INSERT INTO listings (id, carbon_credit_id, seller_id, title, description, listing_type,
                      price, quantity, unit, status, approved_by, approved_at,
                      reject_reason, created_at, updated_at)
VALUES
-- Fixed-price listing for credit #1
(
    1,
    1,              -- carbon_credits.id
    5,              -- owner@example.com
    'EV Trip Hanoi - Noi Bai Credits',
    'Carbon credits generated from verified EV trip Hanoi - Noi Bai.',
    'FIXED_PRICE',
    10.00,
    15.00,
    'CREDITS',
    'APPROVED',
    1,              -- admin approved
    NOW() - INTERVAL 2 DAY,
    NULL,
    NOW() - INTERVAL 2 DAY,
    NOW() - INTERVAL 1 DAY
),
-- Auction listing for credit #2
(
    2,
    2,
    6,              -- owner2@example.com
    'Premium EV Trip Da Nang - Hoi An Credits',
    'High-quality credits from frequent EV journeys between Da Nang and Hoi An.',
    'AUCTION',
    12.00,
    20.00,
    'CREDITS',
    'APPROVED',
    1,
    NOW() - INTERVAL 2 DAY,
    NULL,
    NOW() - INTERVAL 2 DAY,
    NOW() - INTERVAL 1 DAY
);

-- Auction details
INSERT INTO auctions (id, listing_id, start_price, step_price, start_time, end_time)
VALUES
(
    1,
    2,                          -- listing 2
    12.00,
    1.00,
    NOW() - INTERVAL 1 DAY,
    NOW() + INTERVAL 6 HOUR
);

-- Bids from buyers
INSERT INTO bids (id, auction_id, buyer_id, bid_price, status, created_at)
VALUES
(
    1,
    1,
    3,                  -- buyer@example.com
    13.00,
    'OUTBID',
    NOW() - INTERVAL 12 HOUR
),
(
    2,
    1,
    4,                  -- buyer2@example.com
    14.50,
    'LEADING',
    NOW() - INTERVAL 6 HOUR
);

-- ============================================================================
-- 6. TRANSACTIONS, PAYMENTS, INVOICES, AUDIT LOGS
-- ============================================================================

-- Main completed transaction: Buyer 1 buys from Listing 1
INSERT INTO transactions (id, transaction_code, buyer_id, buyer_email, seller_email,
                          listing_id, quantity, total_amount, status, type,
                          created_at, updated_at)
VALUES
(
    1,
    'TX-2025-0001',
    3,                      -- buyer@example.com
    'buyer@example.com',
    'owner@example.com',
    1,                      -- listing 1
    10.00,
    100.00,                 -- 10 credits * 10 USD
    'COMPLETED',
    'CREDIT_PURCHASE',
    NOW() - INTERVAL 1 DAY,
    NOW() - INTERVAL 12 HOUR
),
(
    2,
    'TX-2025-0002',
    4,                      -- buyer2@example.com
    'buyer2@example.com',
    'owner2@example.com',
    2,                      -- auction listing
    5.00,
    72.50,                  -- 5 credits * 14.5 USD
    'PENDING',
    'CREDIT_PURCHASE',
    NOW() - INTERVAL 6 HOUR,
    NOW() - INTERVAL 6 HOUR
);

-- Transaction audit logs
INSERT INTO transaction_audit_logs (id, transaction_id, transaction_code,
                                    old_status, new_status, changed_by,
                                    changed_at, reason)
VALUES
(
    1,
    1,
    'TX-2025-0001',
    'PENDING',
    'COMPLETED',
    'admin@gmail.com',
    NOW() - INTERVAL 12 HOUR,
    'Payment confirmed via gateway.'
),
(
    2,
    2,
    'TX-2025-0002',
    'PENDING',
    'PENDING',
    'admin@gmail.com',
    NOW() - INTERVAL 5 HOUR,
    'Awaiting payment confirmation.'
);

-- Payments
INSERT INTO payments (id, transaction_id, method, status, payment_gateway_ref,
                      amount, created_at)
VALUES
(
    1,
    1,
    'CREDIT_CARD',
    'SUCCESS',
    'GATEWAY-PAY-0001',
    100.00,
    NOW() - INTERVAL 1 DAY
);

-- Invoices
INSERT INTO invoices (id, transaction_id, issue_date, file_path)
VALUES
(
    1,
    1,
    NOW() - INTERVAL 23 HOUR,
    '/invoices/TX-2025-0001.pdf'
);

-- ============================================================================
-- 7. DISPUTES & NOTIFICATIONS
-- ============================================================================

-- Dispute opened by Buyer 1 for transaction 1
INSERT INTO disputes (id, dispute_code, transaction_id, raised_by_user_id,
                      description, admin_note, status, created_at, updated_at)
VALUES
(
    1,
    'DSP-2025-0001',
    1,
    3,      -- buyer@example.com
    'I received fewer credits than expected in my wallet.',
    'Under investigation by support team.',
    'OPEN',
    NOW() - INTERVAL 10 HOUR,
    NOW() - INTERVAL 9 HOUR
);

-- Notifications
INSERT INTO notifications (id, user_id, message, created_at, read_at)
VALUES
(
    1,
    3,  -- buyer@example.com
    'Your purchase TX-2025-0001 has been completed successfully.',
    NOW() - INTERVAL 22 HOUR,
    NOW() - INTERVAL 21 HOUR
),
(
    2,
    5,  -- owner@example.com
    'You have received a payout request confirmation.',
    NOW() - INTERVAL 4 DAY,
    NULL
),
(
    3,
    1,  -- admin
    'New dispute DSP-2025-0001 has been created.',
    NOW() - INTERVAL 9 HOUR,
    NULL
);

-- ============================================================================
-- 8. SETTINGS (SYSTEM CONFIG)
-- ============================================================================

INSERT INTO settings (id, key_name, value, description, updated_at)
VALUES
(
    1,
    'BASE_CARBON_PRICE_USD',
    '10.0',
    'Base price per carbon credit in USD.',
    NOW()
),
(
    2,
    'PLATFORM_FEE_RATE',
    '0.05',
    'Platform fee rate (5%) applied on each completed transaction.',
    NOW()
),
(
    3,
    'MIN_PAYOUT_AMOUNT_USD',
    '50.0',
    'Minimum payout amount for e-wallet withdrawals.',
    NOW()
);

-- ============================================================================
-- 9. AUDIT LOGS & HTTP AUDIT
-- ============================================================================

INSERT INTO audit_logs (id, actor_id, actor_role, action, target_type, target_id,
                        details, ip, user_agent, created_at)
VALUES
(
    1,
    1,                      -- admin
    'ADMIN',
    'LOGIN_SUCCESS',
    'USER',
    '1',
    JSON_OBJECT('email', 'admin@gmail.com'),
    '127.0.0.1',
    'Demo Browser',
    NOW() - INTERVAL 1 DAY
),
(
    2,
    1,
    'ADMIN',
    'UPDATE_TRANSACTION_STATUS',
    'TRANSACTION',
    '1',
    JSON_OBJECT('oldStatus', 'PENDING', 'newStatus', 'COMPLETED'),
    '127.0.0.1',
    'Demo Browser',
    NOW() - INTERVAL 12 HOUR
);

INSERT INTO http_audit_logs (id, username, method, endpoint, action,
                             ip, request_body, status, created_at)
VALUES
(
    1,
    'admin@gmail.com',
    'GET',
    '/api/admin/users',
    'LIST_USERS',
    '127.0.0.1',
    NULL,
    200,
    NOW() - INTERVAL 23 HOUR
),
(
    2,
    'admin@gmail.com',
    'PUT',
    '/api/admin/transactions/1/status',
    'UPDATE_TRANSACTION_STATUS',
    '127.0.0.1',
    '{"status":"COMPLETED"}',
    200,
    NOW() - INTERVAL 12 HOUR
);

-- ============================================================================
-- 10. REFRESH TOKENS & REPORT HISTORY
-- ============================================================================

-- Demo refresh token for Buyer 1 (chủ yếu để FE thấy có dữ liệu)
INSERT INTO refresh_tokens (id, token, user_id, expires_at, revoked, created_at)
VALUES
(
    1,
    'demo-refresh-token-0001',
    3,                                  -- buyer@example.com
    NOW() + INTERVAL 7 DAY,
    FALSE,
    NOW() - INTERVAL 1 DAY
);

-- Report history demo
INSERT INTO report_history (id, type, generated_by, generated_at,
                            start_date, end_date, format, file_path, parameters)
VALUES
(
    1,
    'MONTHLY_SUMMARY',
    1,              -- admin
    NOW() - INTERVAL 1 DAY,
    DATE_SUB(CURDATE(), INTERVAL 30 DAY),
    CURDATE(),
    'PDF',
    '/reports/monthly-summary-2025-10.pdf',
    'Last 30 days platform summary report'
);

-- ============================================================================
-- 11. LEGACY TABLES (OPTIONAL MINIMAL DEMO DATA)
-- ============================================================================

-- Minimal legacy EV owner + wallet + credit transaction

INSERT INTO ev_owners (id)
VALUES (1);

INSERT INTO wallets (id, owner_id, total_balance, locked_balance)
VALUES
(
    1,
    1,
    100.0000,
    0.0000
);

INSERT INTO carbon_credit_transactions (id, carbon_credit_id, source_wallet_id,
                                        destination_wallet_id, transaction_type,
                                        amount, timestamp)
VALUES
(
    1,
    1,          -- carbon_credits.id
    NULL,
    1,          -- wallets.id
    'LEGACY_IMPORT',
    15.0000,
    NOW() - INTERVAL 10 DAY
);

SET FOREIGN_KEY_CHECKS = 1;
