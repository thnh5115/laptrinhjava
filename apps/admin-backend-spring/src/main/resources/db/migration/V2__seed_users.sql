-- V2__seed_users.sql
-- Seed data for 4 default users (one for each role)
-- Password for all accounts: "password123" (BCrypt hashed)

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Insert seed users (one for each role)
INSERT INTO users (email, password_hash, full_name, status, role_id, created_at)
VALUES
  ('admin@carboncredit.com',
   '$2a$10$hP3sQqIPlUABBXiRMy1qVOCjSt3Y43J1el0ZbEpgrkOKTD6xwMJmO',
   'System Administrator','ACTIVE',(SELECT id FROM roles WHERE name='ADMIN'), NOW()),
  ('auditor@carboncredit.com',
   '$2a$10$hP3sQqIPlUABBXiRMy1qVOCjSt3Y43J1el0ZbEpgrkOKTD6xwMJmO',
   'Carbon Verification Authority','ACTIVE',(SELECT id FROM roles WHERE name='AUDITOR'), NOW()),
  ('buyer@carboncredit.com',
   '$2a$10$hP3sQqIPlUABBXiRMy1qVOCjSt3Y43J1el0ZbEpgrkOKTD6xwMJmO',
   'Carbon Credit Buyer','ACTIVE',(SELECT id FROM roles WHERE name='BUYER'), NOW()),
  ('evowner@carboncredit.com',
   '$2a$10$hP3sQqIPlUABBXiRMy1qVOCjSt3Y43J1el0ZbEpgrkOKTD6xwMJmO',
   'Electric Vehicle Owner','ACTIVE',(SELECT id FROM roles WHERE name='EV_OWNER'), NOW())
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  full_name     = VALUES(full_name),
  status        = VALUES(status),
  role_id       = VALUES(role_id);

-- Create e-wallets for all seeded users
INSERT INTO e_wallets (user_id, balance, currency, status, updated_at)
SELECT
    u.id as user_id,
    CASE
        WHEN r.name = 'ADMIN' THEN 10000.00
        WHEN r.name = 'AUDITOR' THEN 5000.00
        WHEN r.name = 'BUYER' THEN 25000.00
        WHEN r.name = 'EV_OWNER' THEN 1000.00
    END as balance,
    'USD' as currency,
    'ACTIVE' as status,
    NOW() as updated_at
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.email IN (
    'admin@carboncredit.com',
    'auditor@carboncredit.com',
    'buyer@carboncredit.com',
    'evowner@carboncredit.com'
)
AND NOT EXISTS (SELECT 1 FROM e_wallets ew WHERE ew.user_id = u.id);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- SEED DATA SUMMARY
-- ============================================================================
-- The following accounts have been created with password: "password123"
--
-- 1. ADMIN ACCOUNT:
--    Email: admin@carboncredit.com
--    Role: System Administrator
--    Wallet Balance: $10,000.00
--
-- 2. AUDITOR ACCOUNT:
--    Email: auditor@carboncredit.com
--    Role: Carbon Verification Authority
--    Wallet Balance: $5,000.00
--
-- 3. BUYER ACCOUNT:
--    Email: buyer@carboncredit.com
--    Role: Carbon Credit Buyer
--    Wallet Balance: $25,000.00
--
-- 4. EV_OWNER ACCOUNT:
--    Email: evowner@carboncredit.com
--    Role: Electric Vehicle Owner
--    Wallet Balance: $1,000.00
--
-- All accounts are ACTIVE and ready for use.
-- ============================================================================