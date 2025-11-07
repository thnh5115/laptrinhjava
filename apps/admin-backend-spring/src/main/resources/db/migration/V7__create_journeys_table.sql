-- ===================================================================
-- Migration: V7__create_journeys_table
-- Description: Create journeys table for EV journey tracking
-- Author: System
-- Date: 2025-11-06
-- ===================================================================

-- Table: journeys
CREATE TABLE journeys (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    journey_date DATE NOT NULL,
    start_location VARCHAR(255),
    end_location VARCHAR(255),
    distance_km DECIMAL(10,2),
    energy_used_kwh DECIMAL(10,2),
    credits_generated DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_by BIGINT,
    verified_at TIMESTAMP NULL,
    rejection_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_journey_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_journey_verifier FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL,
    
    INDEX idx_journey_user (user_id),
    INDEX idx_journey_status (status),
    INDEX idx_journey_date (journey_date DESC),
    INDEX idx_journey_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================================================
-- Section: Sample Journey Data for Testing
-- ===================================================================

-- Sample journeys for buyer@test.local (assuming user_id from V2 migration)
INSERT INTO journeys (user_id, journey_date, start_location, end_location, distance_km, energy_used_kwh, credits_generated, status, created_at)
SELECT 
    u.id,
    DATE_SUB(CURDATE(), INTERVAL 5 DAY),
    'San Francisco, CA',
    'San Jose, CA',
    48.5,
    12.8,
    15.2,
    'VERIFIED',
    DATE_SUB(NOW(), INTERVAL 5 DAY)
FROM users u WHERE u.email = 'buyer@test.local' LIMIT 1;

INSERT INTO journeys (user_id, journey_date, start_location, end_location, distance_km, energy_used_kwh, credits_generated, status, created_at)
SELECT 
    u.id,
    DATE_SUB(CURDATE(), INTERVAL 4 DAY),
    'Los Angeles, CA',
    'San Diego, CA',
    120.0,
    28.5,
    35.8,
    'VERIFIED',
    DATE_SUB(NOW(), INTERVAL 4 DAY)
FROM users u WHERE u.email = 'buyer@test.local' LIMIT 1;

INSERT INTO journeys (user_id, journey_date, start_location, end_location, distance_km, energy_used_kwh, credits_generated, status, created_at)
SELECT 
    u.id,
    DATE_SUB(CURDATE(), INTERVAL 3 DAY),
    'Seattle, WA',
    'Portland, OR',
    174.0,
    42.3,
    52.1,
    'PENDING',
    DATE_SUB(NOW(), INTERVAL 3 DAY)
FROM users u WHERE u.email = 'buyer@test.local' LIMIT 1;

-- Sample journeys for seller@test.local
INSERT INTO journeys (user_id, journey_date, start_location, end_location, distance_km, energy_used_kwh, credits_generated, status, created_at)
SELECT 
    u.id,
    DATE_SUB(CURDATE(), INTERVAL 2 DAY),
    'Boston, MA',
    'New York, NY',
    215.0,
    52.0,
    64.5,
    'VERIFIED',
    DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM users u WHERE u.email = 'seller@test.local' LIMIT 1;

INSERT INTO journeys (user_id, journey_date, start_location, end_location, distance_km, energy_used_kwh, credits_generated, status, created_at)
SELECT 
    u.id,
    DATE_SUB(CURDATE(), INTERVAL 1 DAY),
    'Austin, TX',
    'Houston, TX',
    165.0,
    39.6,
    49.5,
    'REJECTED',
    DATE_SUB(NOW(), INTERVAL 1 DAY)
FROM users u WHERE u.email = 'seller@test.local' LIMIT 1;

-- Update verified journeys with verifier info (use admin user as verifier)
UPDATE journeys j
INNER JOIN users u ON u.email = 'admin@test.local'
SET j.verified_by = u.id,
    j.verified_at = DATE_ADD(j.created_at, INTERVAL 2 HOUR)
WHERE j.status IN ('VERIFIED', 'REJECTED');

-- Add rejection reason for rejected journey
UPDATE journeys
SET rejection_reason = 'Insufficient evidence provided for distance claimed. Please provide GPS tracking data.'
WHERE status = 'REJECTED';

-- ===================================================================
-- End of Migration
-- ===================================================================
