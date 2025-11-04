-- ============================================================================
-- V1: Initial schema for Carbon Verification Authority service
-- ============================================================================
-- Contains verification request ledger and credit issuance tables
-- ============================================================================

CREATE TABLE cva_verification_requests (
    id BINARY(16) PRIMARY KEY,
    owner_id BINARY(16) NOT NULL,
    trip_id VARCHAR(100) NOT NULL,
    distance_km DECIMAL(12,3) NOT NULL,
    energy_kwh DECIMAL(12,3) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    verified_at TIMESTAMP(6) NULL,
    verifier_id BINARY(16) NULL,
    notes TEXT NULL,
    UNIQUE KEY uq_cva_requests_checksum (checksum),
    INDEX idx_cva_requests_owner (owner_id),
    INDEX idx_cva_requests_status (status),
    INDEX idx_cva_requests_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Carbon verification requests awaiting CVA decision';

CREATE TABLE cva_credit_issuances (
    id BINARY(16) PRIMARY KEY,
    request_id BINARY(16) NOT NULL,
    owner_id BINARY(16) NOT NULL,
    co2_reduced_kg DECIMAL(18,6) NOT NULL,
    credits_raw DECIMAL(18,6) NOT NULL,
    credits_rounded DECIMAL(18,2) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    correlation_id VARCHAR(100) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    UNIQUE KEY uq_cva_issuances_idempotency (idempotency_key),
    INDEX idx_cva_issuances_owner (owner_id),
    INDEX idx_cva_issuances_request (request_id),
    CONSTRAINT fk_cva_issuances_request FOREIGN KEY (request_id)
        REFERENCES cva_verification_requests(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Issued carbon credits tied to verification approvals';
