-- V8: Create carbon_credits table and sample data

CREATE TABLE carbon_credits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL COMMENT 'EV owner who generated the credit',
    journey_id BIGINT NOT NULL UNIQUE COMMENT 'Journey that generated this credit',
    amount DECIMAL(10,2) NOT NULL COMMENT 'Amount of credits in tons CO2',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE, LISTED, SOLD, RESERVED',
    price_per_credit DECIMAL(10,2) COMMENT 'Price when listed on marketplace',
    listed_at TIMESTAMP NULL COMMENT 'When credit was listed for sale',
    sold_at TIMESTAMP NULL COMMENT 'When credit was sold',
    buyer_id BIGINT COMMENT 'Buyer who purchased the credit',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_credit_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_journey FOREIGN KEY (journey_id) REFERENCES journeys(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_credit_owner (owner_id),
    INDEX idx_credit_status (status),
    INDEX idx_credit_journey (journey_id),
    INDEX idx_credit_created_at (created_at DESC),
    INDEX idx_credit_listed_at (listed_at DESC)
);

-- Sample carbon credits for verified journeys
-- Journey 1 (buyer@test.local, SF→SJ, 80.5km, 16.1kWh) → 12.08 tons CO2 saved → SOLD
-- Journey 2 (buyer@test.local, LA→SD, 193.12km, 38.62kWh) → 28.97 tons CO2 saved → LISTED
-- Journey 4 (seller@test.local, BOS→NYC, 346.41km, 69.28kWh) → 51.96 tons CO2 saved → AVAILABLE

-- Credit from Journey 1 - SOLD to seller@test.local
INSERT INTO carbon_credits (owner_id, journey_id, amount, status, price_per_credit, listed_at, sold_at, buyer_id, created_at)
SELECT
    j.user_id AS owner_id,
    j.id AS journey_id,
    j.credits_generated AS amount,
    'SOLD' AS status,
    25.00 AS price_per_credit,
    DATE_SUB(NOW(), INTERVAL 5 DAY) AS listed_at,
    DATE_SUB(NOW(), INTERVAL 2 DAY) AS sold_at,
    (SELECT id FROM users WHERE email = 'seller@test.local') AS buyer_id,
    DATE_SUB(NOW(), INTERVAL 6 DAY) AS created_at
FROM journeys j
WHERE j.id = (SELECT id FROM journeys WHERE user_id = (SELECT id FROM users WHERE email = 'buyer@test.local') ORDER BY id LIMIT 1);

-- Credit from Journey 2 - LISTED at $30/credit
INSERT INTO carbon_credits (owner_id, journey_id, amount, status, price_per_credit, listed_at, sold_at, buyer_id, created_at)
SELECT
    j.user_id AS owner_id,
    j.id AS journey_id,
    j.credits_generated AS amount,
    'LISTED' AS status,
    30.00 AS price_per_credit,
    DATE_SUB(NOW(), INTERVAL 3 DAY) AS listed_at,
    NULL AS sold_at,
    NULL AS buyer_id,
    DATE_SUB(NOW(), INTERVAL 4 DAY) AS created_at
FROM journeys j
WHERE j.id = (SELECT id FROM journeys WHERE user_id = (SELECT id FROM users WHERE email = 'buyer@test.local') ORDER BY id LIMIT 1 OFFSET 1);

-- Credit from Journey 4 - AVAILABLE (not yet listed)
INSERT INTO carbon_credits (owner_id, journey_id, amount, status, price_per_credit, listed_at, sold_at, buyer_id, created_at)
SELECT
    j.user_id AS owner_id,
    j.id AS journey_id,
    j.credits_generated AS amount,
    'AVAILABLE' AS status,
    NULL AS price_per_credit,
    NULL AS listed_at,
    NULL AS sold_at,
    NULL AS buyer_id,
    DATE_SUB(NOW(), INTERVAL 1 DAY) AS created_at
FROM journeys j
WHERE j.id = (SELECT id FROM journeys WHERE user_id = (SELECT id FROM users WHERE email = 'seller@test.local') ORDER BY id LIMIT 1);

-- Verify sample data
-- Expected results:
-- 3 credits total
-- 1 AVAILABLE (from seller@test.local journey)
-- 1 LISTED (from buyer@test.local journey, price $30)
-- 1 SOLD (from buyer@test.local journey to seller@test.local, price $25)
