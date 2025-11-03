CREATE TABLE verification_requests (
    id BINARY(16) NOT NULL,
    owner_id BINARY(16) NOT NULL,
    trip_id VARCHAR(128) NOT NULL,
    distance_km DECIMAL(10,2) NOT NULL,
    energy_kwh DECIMAL(10,2) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    verifier_id BINARY(16) NULL,
    notes VARCHAR(1000) NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_verification_requests_checksum ON verification_requests(checksum);
CREATE INDEX idx_verification_requests_owner ON verification_requests(owner_id);
CREATE INDEX idx_verification_requests_status ON verification_requests(status);
CREATE INDEX idx_verification_requests_created_at ON verification_requests(created_at);

CREATE TABLE credit_issuances (
    id BINARY(16) NOT NULL,
    request_id BINARY(16) NOT NULL,
    owner_id BINARY(16) NOT NULL,
    co2_reduced_kg DECIMAL(16,6) NOT NULL,
    credits_raw DECIMAL(18,6) NOT NULL,
    credits_rounded DECIMAL(18,2) NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    correlation_id VARCHAR(64) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_credit_issuances_request FOREIGN KEY (request_id)
        REFERENCES verification_requests(id)
        ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_credit_issuances_idempotency ON credit_issuances(idempotency_key);
CREATE UNIQUE INDEX uk_credit_issuances_request ON credit_issuances(request_id);
CREATE INDEX idx_credit_issuances_owner ON credit_issuances(owner_id);
