-- ============================================================================
-- Migration: V12__cva_merge_into_admin
-- Purpose  : Consolidate CVA verification tables into ADMIN schema
-- Actions  :
--   * Ensure journeys table holds all CVA verification metadata
--   * Ensure carbon_credits table enforces 1-1 with journeys and reporting indexes
--   * Drop legacy CVA verification tables (verification_requests / credit_issuances)
-- ============================================================================

-- --------------------------------------------------------------------------
-- Journeys table hardening: add missing CVA verification columns (idempotent)
-- --------------------------------------------------------------------------
ALTER TABLE journeys
    ADD COLUMN IF NOT EXISTS verified_by BIGINT NULL,
    ADD COLUMN IF NOT EXISTS verified_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS rejection_reason TEXT NULL,
    ADD COLUMN IF NOT EXISTS credits_generated DECIMAL(10,2) NULL;

-- Foreign key to users for verifier (add only if missing)
-- Composite index to list pending journeys ordered by submission time
CREATE INDEX idx_journeys_status_created
    ON journeys (status, created_at);

-- --------------------------------------------------------------------------
-- Carbon credits table (ensure existence + idempotent uniqueness/indexes)
-- --------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS carbon_credits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    journey_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    price_per_credit DECIMAL(10,2) NULL,
    listed_at TIMESTAMP NULL,
    sold_at TIMESTAMP NULL,
    buyer_id BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_journey FOREIGN KEY (journey_id) REFERENCES journeys(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Ensure 1-1 mapping between journey and credit even on existing tables
ALTER TABLE carbon_credits DROP INDEX IF EXISTS journey_id;
ALTER TABLE carbon_credits
    ADD CONSTRAINT uk_carbon_credits_journey_id UNIQUE (journey_id);

-- Helpful covering indexes for reporting/statistics
CREATE INDEX idx_credit_owner_status
    ON carbon_credits (owner_id, status);
CREATE INDEX idx_credit_status_created
    ON carbon_credits (status, created_at);

-- --------------------------------------------------------------------------
-- Clean up deprecated CVA-specific tables now that data lives in ADMIN schema
-- --------------------------------------------------------------------------
DROP TABLE IF EXISTS credit_issuances;
DROP TABLE IF EXISTS verification_requests;

-- ============================================================================
-- End of Migration
-- ============================================================================
