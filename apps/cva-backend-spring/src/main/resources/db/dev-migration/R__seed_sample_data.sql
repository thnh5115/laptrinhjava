-- Seed data aligned with the shared Admin schema (journeys + carbon_credits).
-- Deterministic identifiers keep the fixtures stable across resets.

SET @owner_a = 10001;
SET @owner_b = 10002;
SET @verifier = 90001;

SET @journey_approved = 20001;
SET @journey_rejected = 20002;
SET @journey_pending = 20003;
SET @journey_recent_pending = 20004;
SET @journey_recent_approved = 20005;

INSERT IGNORE INTO journeys (
    id,
    user_id,
    journey_date,
    start_location,
    end_location,
    distance_km,
    energy_used_kwh,
    credits_generated,
    status,
    verified_by,
    verified_at,
    rejection_reason,
    created_at
)
VALUES
    (@journey_approved, @owner_a, DATE(UTC_TIMESTAMP(6) - INTERVAL 28 DAY), 'Ha Noi', 'Hai Phong', 125.750, 42.330, 18.28, 'VERIFIED', @verifier, UTC_TIMESTAMP(6) - INTERVAL 27 DAY, NULL, UTC_TIMESTAMP(6) - INTERVAL 28 DAY),
    (@journey_rejected, @owner_b, DATE(UTC_TIMESTAMP(6) - INTERVAL 21 DAY), 'Da Nang', 'Hoi An', 98.120, 37.810, NULL, 'REJECTED', @verifier, UTC_TIMESTAMP(6) - INTERVAL 20 DAY, 'Emission data incomplete', UTC_TIMESTAMP(6) - INTERVAL 21 DAY),
    (@journey_pending, @owner_a, DATE(UTC_TIMESTAMP(6) - INTERVAL 14 DAY), 'Can Tho', 'Ca Mau', 85.000, 29.500, NULL, 'PENDING', NULL, NULL, 'Awaiting telemetry files', UTC_TIMESTAMP(6) - INTERVAL 14 DAY),
    (@journey_recent_pending, @owner_b, DATE(UTC_TIMESTAMP(6) - INTERVAL 3 DAY), 'Quy Nhon', 'Phu Yen', 64.420, 18.750, NULL, 'PENDING', NULL, NULL, 'Queued for review', UTC_TIMESTAMP(6) - INTERVAL 3 DAY),
    (@journey_recent_approved, @owner_a, DATE(UTC_TIMESTAMP(6) - INTERVAL 5 DAY), 'Ho Chi Minh City', 'Vung Tau', 132.340, 43.220, 14.90, 'VERIFIED', @verifier, UTC_TIMESTAMP(6) - INTERVAL 4 DAY, NULL, UTC_TIMESTAMP(6) - INTERVAL 5 DAY);

INSERT IGNORE INTO carbon_credits (
    id,
    owner_id,
    journey_id,
    amount,
    status,
    price_per_credit,
    listed_at,
    sold_at,
    buyer_id,
    created_at
)
VALUES
    (30001, @owner_a, @journey_approved, 18.28, 'AVAILABLE', NULL, NULL, NULL, NULL, UTC_TIMESTAMP(6) - INTERVAL 27 DAY),
    (30002, @owner_a, @journey_recent_approved, 14.90, 'AVAILABLE', NULL, NULL, NULL, NULL, UTC_TIMESTAMP(6) - INTERVAL 4 DAY);
