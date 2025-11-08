-- ====================================================================
--  Carbon Credit Marketplace - Buyer Backend (Full Schema)
-- ====================================================================
SET NAMES utf8mb4;
SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS = 0;

-- =========================================================
-- USERS / BUYERS
-- =========================================================
CREATE TABLE IF NOT EXISTS buyers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    balance DECIMAL(19,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- LISTINGS (carbon credit listings)
-- =========================================================
CREATE TABLE IF NOT EXISTS listings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    quantity DECIMAL(19,4) NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    total_value DECIMAL(19,4) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',       -- ACTIVE, SOLD, CANCELLED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- AUCTIONS
-- =========================================================
CREATE TABLE IF NOT EXISTS auctions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    listing_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    current_price DECIMAL(19,4) DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',         -- OPEN, CLOSED, CANCELLED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_auction_listing FOREIGN KEY (listing_id)
        REFERENCES listings(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_auction_listing ON auctions(listing_id);

-- =========================================================
-- BIDS
-- =========================================================
CREATE TABLE IF NOT EXISTS bids (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    bid_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bid_auction FOREIGN KEY (auction_id)
        REFERENCES auctions(id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_bid_buyer FOREIGN KEY (buyer_id)
        REFERENCES buyers(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bid_auction ON bids(auction_id);
CREATE INDEX idx_bid_buyer ON bids(buyer_id);

-- =========================================================
-- CREDIT ORDERS
-- =========================================================
CREATE TABLE IF NOT EXISTS credit_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    buyer_id BIGINT NOT NULL,
    quantity DECIMAL(19,4) NOT NULL,
    unit_price DECIMAL(19,4) NOT NULL,
    total_amount DECIMAL(19,4) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',      -- PENDING, APPROVED, REJECTED, COMPLETED
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_credit_orders_buyer FOREIGN KEY (buyer_id)
        REFERENCES buyers(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- TRANSACTIONS
-- =========================================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    credit_order_id BIGINT NULL,
    listing_id BIGINT NULL,
    tx_type VARCHAR(30) NOT NULL,                       -- CREDIT_PURCHASE, REFUND, AUCTION_PAYMENT
    tx_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(19,4) NOT NULL,
    metadata JSON NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tx_credit_order FOREIGN KEY (credit_order_id)
        REFERENCES credit_orders(id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_tx_listing FOREIGN KEY (listing_id)
        REFERENCES listings(id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- PAYMENTS
-- =========================================================
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tx_id BIGINT NOT NULL,
    method VARCHAR(50) NOT NULL,                        -- VNPAY, MOMO, BANK_TRANSFER
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',      -- PENDING, SUCCESS, FAILED
    ref VARCHAR(100),
    amount DECIMAL(19,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_tx FOREIGN KEY (tx_id)
        REFERENCES transactions(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- INVOICES
-- =========================================================
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tx_id BIGINT NOT NULL,
    issue_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_path VARCHAR(255) NOT NULL,
    CONSTRAINT fk_invoice_tx FOREIGN KEY (tx_id)
        REFERENCES transactions(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- NOTIFICATIONS
-- =========================================================
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    buyer_id BIGINT NOT NULL,
    message VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL,
    CONSTRAINT fk_notification_buyer FOREIGN KEY (buyer_id)
        REFERENCES buyers(id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- AUDIT LOGS
-- =========================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by VARCHAR(150),
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
