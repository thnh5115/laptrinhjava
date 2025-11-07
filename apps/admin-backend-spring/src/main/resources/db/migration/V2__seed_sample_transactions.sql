-- =========================
-- Seed users (buyer/seller)
-- =========================

-- Buyer user mẫu (role BUYER), password = "password" (bcrypt)
INSERT INTO users (email, password_hash, full_name, status, role_id, created_at)
SELECT 
  'buyer@test.local',
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
  'Sample Buyer',
  'ACTIVE',
  (SELECT id FROM roles WHERE name = 'BUYER' LIMIT 1),
  NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'buyer@test.local');

-- Seller user mẫu (role EV_OWNER), password = "password" (bcrypt)
INSERT INTO users (email, password_hash, full_name, status, role_id, created_at)
SELECT 
  'seller@test.local',
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
  'Sample EV Owner',
  'ACTIVE',
  (SELECT id FROM roles WHERE name = 'EV_OWNER' LIMIT 1),
  NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'seller@test.local');

-- =========================
-- Sample transactions
-- =========================
-- Tạo nhiều transactions để test pagination, filter, sort
INSERT INTO transactions (
  transaction_code,
  buyer_email,
  seller_email,
  amount,
  total_price,
  status,
  type,
  version,
  created_at,
  updated_at
) VALUES
  -- Transaction 1: PENDING
  (CONCAT('TX-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0001'),
   'buyer@test.local',
   'seller@test.local',
   100.00, 1500.00, 'PENDING', 'CREDIT_PURCHASE', 0, NOW(), NOW()),
  
  -- Transaction 2: APPROVED
  (CONCAT('TX-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0002'),
   'buyer@test.local',
   'seller@test.local',
   200.00, 3200.00, 'APPROVED', 'CREDIT_PURCHASE', 0, NOW() - INTERVAL 1 DAY, NOW()),
  
  -- Transaction 3: REJECTED
  (CONCAT('TX-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0003'),
   'buyer@test.local',
   'seller@test.local',
   50.00, 750.00, 'REJECTED', 'CREDIT_SALE', 0, NOW() - INTERVAL 2 DAY, NOW()),
  
  -- Transaction 4: PENDING (for testing update)
  (CONCAT('TX-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0004'),
   'buyer@test.local',
   'seller@test.local',
   150.00, 2250.00, 'PENDING', 'TRANSFER', 0, NOW() - INTERVAL 3 HOUR, NOW()),
  
  -- Transaction 5: APPROVED (higher amount for sort testing)
  (CONCAT('TX-', DATE_FORMAT(NOW(), '%Y%m%d'), '-0005'),
   'buyer@test.local',
   'seller@test.local',
   500.00, 8000.00, 'APPROVED', 'CREDIT_PURCHASE', 0, NOW() - INTERVAL 5 DAY, NOW());