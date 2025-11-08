-- Ensure verification status and timestamps have sensible defaults even when service-level hooks are bypassed.
ALTER TABLE verification_requests
    MODIFY COLUMN status VARCHAR(32) NOT NULL DEFAULT 'PENDING';

ALTER TABLE verification_requests
    MODIFY COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);

UPDATE verification_requests
SET status = 'PENDING'
WHERE status IS NULL;

ALTER TABLE credit_issuances
    MODIFY COLUMN created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
