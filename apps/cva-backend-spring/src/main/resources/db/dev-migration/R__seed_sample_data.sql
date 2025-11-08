-- Seed data to make the CVA dashboard usable in development profiles.
-- These inserts rely on deterministic UUIDs so they can be referenced by follow-up scripts.

SET @owner_a = UUID_TO_BIN('11111111-2222-3333-4444-555555555555');
SET @owner_b = UUID_TO_BIN('66666666-7777-8888-9999-aaaaaaaaaaaa');
SET @verifier = UUID_TO_BIN('aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee');

SET @req_approved = UUID_TO_BIN('00000000-0000-0000-0000-000000000101');
SET @req_rejected = UUID_TO_BIN('00000000-0000-0000-0000-000000000102');
SET @req_pending = UUID_TO_BIN('00000000-0000-0000-0000-000000000103');
SET @req_recent_pending = UUID_TO_BIN('00000000-0000-0000-0000-000000000104');
SET @req_recent_approved = UUID_TO_BIN('00000000-0000-0000-0000-000000000105');

INSERT IGNORE INTO verification_requests (id, owner_id, trip_id, distance_km, energy_kwh, checksum, status, created_at, verified_at, verifier_id, notes)
VALUES
    (@req_approved, @owner_a, 'DEV-SEED-001', 125.750, 42.330, 'seed-checksum-001', 'APPROVED', UTC_TIMESTAMP(6) - INTERVAL 28 DAY, UTC_TIMESTAMP(6) - INTERVAL 27 DAY, @verifier, 'Baseline approval seed'),
    (@req_rejected, @owner_b, 'DEV-SEED-002', 98.120, 37.810, 'seed-checksum-002', 'REJECTED', UTC_TIMESTAMP(6) - INTERVAL 21 DAY, UTC_TIMESTAMP(6) - INTERVAL 20 DAY, @verifier, 'Emission data incomplete'),
    (@req_pending, @owner_a, 'DEV-SEED-003', 85.000, 29.500, 'seed-checksum-003', 'PENDING', UTC_TIMESTAMP(6) - INTERVAL 14 DAY, NULL, NULL, 'Awaiting telemetry files'),
    (@req_recent_pending, @owner_b, 'DEV-SEED-004', 64.420, 18.750, 'seed-checksum-004', 'PENDING', UTC_TIMESTAMP(6) - INTERVAL 3 DAY, NULL, NULL, 'Queued for review'),
    (@req_recent_approved, @owner_a, 'DEV-SEED-005', 132.340, 43.220, 'seed-checksum-005', 'APPROVED', UTC_TIMESTAMP(6) - INTERVAL 5 DAY, UTC_TIMESTAMP(6) - INTERVAL 4 DAY, @verifier, 'Remote validation successful');

INSERT IGNORE INTO credit_issuances (id, request_id, owner_id, co2_reduced_kg, credits_raw, credits_rounded, idempotency_key, correlation_id, created_at)
VALUES
    (UUID_TO_BIN('00000000-0000-0000-0000-000000000201'), @req_approved, @owner_a, 456.789000, 18.275000, 18.28, 'DEV-IDEMPOTENCY-001', 'DEV-CORRELATION-001', UTC_TIMESTAMP(6) - INTERVAL 27 DAY),
    (UUID_TO_BIN('00000000-0000-0000-0000-000000000202'), @req_recent_approved, @owner_a, 372.440000, 14.897000, 14.90, 'DEV-IDEMPOTENCY-002', 'DEV-CORRELATION-002', UTC_TIMESTAMP(6) - INTERVAL 4 DAY);
