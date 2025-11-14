SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- MODULE 1: USER MANAGEMENT & AUTHENTICATION
-- ============================================================================

-- Table: roles (User roles)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed default roles
INSERT INTO roles (name, description)
SELECT * FROM (
    SELECT 'ADMIN',    'System Administrator' UNION ALL
    SELECT 'AUDITOR',  'Carbon Verification Authority' UNION ALL
    SELECT 'BUYER',    'Carbon Credit Buyer' UNION ALL
    SELECT 'EV_OWNER', 'Electric Vehicle Owner'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM roles LIMIT 1);

-- Table: users (Master user table)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120),
    status ENUM('ACTIVE', 'SUSPENDED', 'BANNED') NOT NULL DEFAULT 'ACTIVE',
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON UPDATE CASCADE,
    INDEX idx_users_role_id (role_id),
    INDEX idx_users_status (status),
    INDEX idx_users_email_lower ((LOWER(email)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MODULE 2: FINANCIAL MANAGEMENT (WALLETS & TRANSACTIONS)
-- ============================================================================

-- Table: e_wallets (Money wallets)
CREATE TABLE IF NOT EXISTS e_wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    status ENUM('ACTIVE', 'FROZEN') NOT NULL DEFAULT 'ACTIVE',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ewallet_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: payouts (Withdrawal requests)
CREATE TABLE IF NOT EXISTS payouts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    e_wallet_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    processed_by BIGINT NULL,
    notes TEXT,
    bank_account VARCHAR(255) COMMENT 'Bank account details',
    payment_method VARCHAR(50) NULL,

    CONSTRAINT fk_payout_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_payout_wallet FOREIGN KEY (e_wallet_id) REFERENCES e_wallets(id),
    CONSTRAINT fk_payout_processor FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_payout_user_status (user_id, status),
    INDEX idx_payout_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: deposits (Deposit transactions)
CREATE TABLE IF NOT EXISTS deposits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    e_wallet_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_gateway_ref VARCHAR(100) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_deposit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_deposit_wallet FOREIGN KEY (e_wallet_id) REFERENCES e_wallets(id),
    INDEX idx_deposit_user (user_id),
    INDEX idx_deposit_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MODULE 3: JOURNEYS & VERIFICATION (FROM OWNER & CVA MODULES)
-- ============================================================================

-- Table: journeys (Trip/journey records)
CREATE TABLE IF NOT EXISTS journeys (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'EV Owner (from users table)',
    journey_date DATE NOT NULL,
    start_location VARCHAR(255),
    end_location VARCHAR(255),
    distance_km DECIMAL(10,2) NOT NULL,
    energy_used_kwh DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_by BIGINT NULL COMMENT 'CVA/Auditor (from users table)',
    verified_at TIMESTAMP NULL,
    rejection_reason TEXT,
    credits_generated DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_journey_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_journey_verifier FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_journey_user_status (user_id, status),
    INDEX idx_journey_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MODULE 4: CARBON CREDITS (MASTER LEDGER)
-- ============================================================================

-- Table: carbon_credits (Carbon credit tokens)
CREATE TABLE IF NOT EXISTS carbon_credits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL COMMENT 'Current owner (from users table)',
    journey_id BIGINT NOT NULL UNIQUE COMMENT 'Source journey',
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    price_per_credit DECIMAL(10,2),
    listed_at TIMESTAMP NULL,
    sold_at TIMESTAMP NULL,
    buyer_id BIGINT NULL COMMENT 'Buyer when sold',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_credit_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_journey FOREIGN KEY (journey_id) REFERENCES journeys(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_credit_owner_status (owner_id, status),
    INDEX idx_credit_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MODULE 5: MARKETPLACE (LISTINGS & AUCTIONS)
-- ============================================================================

-- Table: listings (Marketplace listings)
CREATE TABLE IF NOT EXISTS listings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    carbon_credit_id BIGINT NOT NULL UNIQUE,
    seller_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    listing_type ENUM('FIXED_PRICE', 'AUCTION') NOT NULL DEFAULT 'FIXED_PRICE',
    price DECIMAL(15, 2) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    reject_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_listing_credit FOREIGN KEY (carbon_credit_id) REFERENCES carbon_credits(id),
    CONSTRAINT fk_listing_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_listing_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_listings_seller_id (seller_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: auctions (Auction details)
CREATE TABLE IF NOT EXISTS auctions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    listing_id BIGINT NOT NULL UNIQUE,
    start_price DECIMAL(15, 2) NOT NULL,
    step_price DECIMAL(15, 2) NOT NULL DEFAULT 1.00,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,

    CONSTRAINT fk_auction_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: bids (Auction bids)
CREATE TABLE IF NOT EXISTS bids (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    bid_price DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'LEADING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bid_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    CONSTRAINT fk_bid_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_bids_auction_buyer (auction_id, buyer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MODULE 6: TRANSACTIONS & PAYMENTS
-- ============================================================================

-- Table: transactions (Financial transactions)
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_code VARCHAR(100) UNIQUE,
    buyer_id BIGINT NOT NULL,
    buyer_email VARCHAR(255),
    seller_email VARCHAR(255),
    listing_id BIGINT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    type VARCHAR(20) NOT NULL DEFAULT 'CREDIT_PURCHASE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tx_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tx_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
    UNIQUE KEY uk_transactions_code (transaction_code),
    INDEX idx_tx_buyer_id (buyer_id),
    INDEX idx_tx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS transaction_audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    transaction_code VARCHAR(100) NOT NULL,
    old_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(500),

    CONSTRAINT fk_tx_audit_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    INDEX idx_tx_audit_transaction (transaction_id),
    INDEX idx_tx_audit_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: payments (Payment details)
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL UNIQUE,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_gateway_ref VARCHAR(100) UNIQUE,
    amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: invoices (Generated invoices)
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL UNIQUE,
    issue_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_path VARCHAR(255) NOT NULL,

    CONSTRAINT fk_invoice_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MODULE 7: SYSTEM TABLES (ADMIN FEATURES)
-- ============================================================================

-- Table: disputes (Transaction disputes)
CREATE TABLE IF NOT EXISTS disputes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispute_code VARCHAR(50) NOT NULL UNIQUE,
    transaction_id BIGINT NOT NULL,
    raised_by_user_id BIGINT NOT NULL,
    description TEXT,
    admin_note TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_dispute_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_dispute_user FOREIGN KEY (raised_by_user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_disputes_status (status),
    INDEX idx_disputes_transaction (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: notifications (User notifications)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    message VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME NULL,

    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: settings (System settings)
CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL UNIQUE,
    value VARCHAR(500) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: audit_logs (Business audit logs)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_id BIGINT NULL,
    actor_role VARCHAR(30) NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50) NULL,
    target_id VARCHAR(100) NULL,
    details JSON NULL,
    ip VARCHAR(45) NULL,
    user_agent VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_created_at (created_at),
    INDEX idx_audit_actor_id (actor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: http_audit_logs (Technical HTTP logs)
CREATE TABLE IF NOT EXISTS http_audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL,
    ip VARCHAR(45),
    request_body LONGTEXT,
    status INT,
    created_at DATETIME(6) NOT NULL,

    INDEX idx_http_audit_created_at (created_at),
    INDEX idx_http_audit_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: refresh_tokens (Authentication tokens)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: report_history (Generated reports)
CREATE TABLE IF NOT EXISTS report_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL,
    generated_by BIGINT NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date DATE,
    end_date DATE,
    format VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    parameters TEXT,

    CONSTRAINT fk_report_history_admin FOREIGN KEY (generated_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_report_history_generated_by (generated_by),
    INDEX idx_report_history_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- LEGACY TABLES (FOR BACKWARD COMPATIBILITY - WILL BE REMOVED LATER)
-- ============================================================================

-- Legacy table: verification_requests (from CVA module)
CREATE TABLE IF NOT EXISTS verification_requests (
    id BINARY(16) PRIMARY KEY,
    owner_id BINARY(16) NOT NULL,
    trip_id VARCHAR(100) NOT NULL,
    distance_km DECIMAL(12, 3) NOT NULL,
    energy_kwh DECIMAL(12, 3) NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    verified_at TIMESTAMP(6) NULL,
    verifier_id BINARY(16) NULL,
    notes TEXT NULL,

    CONSTRAINT uk_verification_requests_checksum UNIQUE (checksum),
    CONSTRAINT uk_verification_requests_owner_trip UNIQUE (owner_id, trip_id),
    INDEX idx_verification_requests_owner (owner_id),
    INDEX idx_verification_requests_status (status),
    INDEX idx_verification_requests_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Legacy table: credit_issuances (from CVA module)
CREATE TABLE IF NOT EXISTS credit_issuances (
    id BINARY(16) PRIMARY KEY,
    request_id BINARY(16) NOT NULL,
    owner_id BINARY(16) NOT NULL,
    co2_reduced_kg DECIMAL(18, 6) NOT NULL,
    credits_raw DECIMAL(18, 6) NOT NULL,
    credits_rounded DECIMAL(18, 2) NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    correlation_id VARCHAR(100) NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_credit_issuances_request FOREIGN KEY (request_id) REFERENCES verification_requests(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uk_credit_issuances_request UNIQUE (request_id),
    CONSTRAINT uk_credit_issuances_idempotency UNIQUE (idempotency_key),
    INDEX idx_credit_issuances_owner (owner_id),
    INDEX idx_credit_issuances_created_at (created_at),
    INDEX idx_credit_issuances_corr (correlation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Legacy table: ev_owners (from Owner module)
CREATE TABLE IF NOT EXISTS ev_owners (
    id BIGINT PRIMARY KEY AUTO_INCREMENT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Legacy table: wallets (from Owner module)
CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    total_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    locked_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,

    CONSTRAINT fk_wallets_owner FOREIGN KEY (owner_id) REFERENCES ev_owners(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uk_wallets_owner UNIQUE (owner_id),
    INDEX idx_wallets_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Legacy table: carbon_credit_transactions (from Owner module)
CREATE TABLE IF NOT EXISTS carbon_credit_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    carbon_credit_id BIGINT NOT NULL,
    source_wallet_id BIGINT NULL,
    destination_wallet_id BIGINT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    timestamp TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_cc_transactions_credit FOREIGN KEY (carbon_credit_id) REFERENCES carbon_credits(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cc_transactions_source_wallet FOREIGN KEY (source_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_cc_transactions_dest_wallet FOREIGN KEY (destination_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_cc_transactions_credit (carbon_credit_id),
    INDEX idx_cc_transactions_source (source_wallet_id),
    INDEX idx_cc_transactions_dest (destination_wallet_id),
    INDEX idx_cc_transactions_type (transaction_type),
    INDEX idx_cc_transactions_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
