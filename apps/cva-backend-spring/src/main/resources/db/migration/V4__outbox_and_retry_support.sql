-- =========================================================================
-- V4: Outbox infrastructure + relational consistency tuning
-- =========================================================================

-- Tighten foreign key semantics for credit issuances to align with retry logic
ALTER TABLE credit_issuances
    DROP FOREIGN KEY fk_credit_issuances_request;

ALTER TABLE credit_issuances
    ADD CONSTRAINT fk_credit_issuances_request
        FOREIGN KEY (request_id)
        REFERENCES verification_requests(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE;

-- Handy composite index for status filtered timeline queries
ALTER TABLE verification_requests
    ADD INDEX idx_verification_requests_status_created (status, created_at);

-- Outbox table backing resilient integrations (wallet + audit)
CREATE TABLE cva_outbox (
    id BINARY(16) PRIMARY KEY,
    type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payload TEXT NOT NULL,
    correlation_id VARCHAR(100) NULL,
    idempotency_key VARCHAR(100) NULL,
    attempts INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    last_error VARCHAR(500) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NULL,
    INDEX idx_outbox_status (status),
    INDEX idx_outbox_next_attempt (next_attempt_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Deferred integration events for CVA outbound calls';
