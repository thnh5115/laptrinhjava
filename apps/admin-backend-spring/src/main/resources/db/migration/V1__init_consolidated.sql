
-- ============================================================================
-- 1. ROLES TABLE (No dependencies)
-- ============================================================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='User roles for RBAC';

-- Seed default roles
INSERT INTO roles (name, description)
SELECT * FROM (
    SELECT 'ADMIN',   'System administrator with full access' UNION ALL
    SELECT 'AUDITOR', 'Verification and audit personnel'     UNION ALL
    SELECT 'BUYER',   'Carbon credit buyer'                  UNION ALL
    SELECT 'EV_OWNER','Electric vehicle owner'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM roles LIMIT 1);

-- ============================================================================
-- 2. USERS TABLE (Depends on: roles)
-- ============================================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120),
    status ENUM('ACTIVE', 'SUSPENDED', 'BANNED') NOT NULL DEFAULT 'ACTIVE',
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role FOREIGN KEY (role_id)
        REFERENCES roles(id) ON UPDATE CASCADE,

    INDEX idx_users_role_id (role_id),
    INDEX idx_users_status (status),
    INDEX idx_users_email_lower ((LOWER(email)))  -- Functional index for case-insensitive search
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='System users with role-based access';

-- Seed default admin user (password: "password")
INSERT INTO users (email, password_hash, full_name, status, role_id)
SELECT
    'admin@gmail.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'Test Admin User',
    'ACTIVE',
    r.id
FROM roles r
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM users u WHERE u.email = 'admin@gmail.com')
LIMIT 1;

-- ============================================================================
-- 3. AUDIT_LOGS TABLE (Depends on: users)
-- ============================================================================
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

    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id)
        REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_audit_created_at (created_at),
    INDEX idx_audit_actor_id (actor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for user actions';

-- ============================================================================
-- 4. HTTP_AUDIT_LOGS TABLE (No dependencies)
-- ============================================================================
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='HTTP request audit logs';

-- ============================================================================
-- 5. LISTINGS TABLE (Depends on: users)
-- ============================================================================
CREATE TABLE IF NOT EXISTS listings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(15,2) NOT NULL,
    quantity DECIMAL(15,2) NOT NULL,
    unit VARCHAR(50) DEFAULT 'kgCO2',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    owner_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_listings_status (status),
    INDEX idx_listings_owner_id (owner_id),
    INDEX idx_listings_created_at (created_at),
    INDEX idx_listings_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Carbon credit listings created by EV owners';

-- ============================================================================
-- 6. TRANSACTIONS TABLE (No FK dependencies)
-- ============================================================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_code VARCHAR(100) UNIQUE NOT NULL,
    buyer_email VARCHAR(255) NOT NULL,
    seller_email VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL COMMENT 'Carbon credit quantity',
    total_price DECIMAL(15,2) NOT NULL COMMENT 'Total transaction value',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' NOT NULL,
    type ENUM('CREDIT_PURCHASE', 'CREDIT_SALE', 'TRANSFER') NOT NULL,
    version BIGINT NOT NULL DEFAULT 0 COMMENT 'Optimistic locking version',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_transaction_buyer (buyer_email),
    INDEX idx_transaction_seller (seller_email),
    INDEX idx_transaction_status (status),
    INDEX idx_transaction_type (type),
    INDEX idx_transaction_created_at (created_at),
    INDEX idx_transaction_code (transaction_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Carbon credit transactions';

-- ============================================================================
-- 7. TRANSACTION_AUDIT_LOGS TABLE (Depends on: transactions)
-- ============================================================================
CREATE TABLE IF NOT EXISTS transaction_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL COMMENT 'Reference to transactions.id',
    transaction_code VARCHAR(100) NOT NULL COMMENT 'Transaction code for quick reference',
    old_status VARCHAR(20) NOT NULL COMMENT 'Previous transaction status',
    new_status VARCHAR(20) NOT NULL COMMENT 'New transaction status',
    changed_by VARCHAR(255) NOT NULL COMMENT 'Email of admin who made the change',
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When the change occurred',
    reason VARCHAR(500) COMMENT 'Optional reason for status change',

    INDEX idx_transaction_id (transaction_id),
    INDEX idx_changed_by (changed_by),
    INDEX idx_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail for transaction status changes';

-- ============================================================================
-- 8. DISPUTES TABLE (Depends on: transactions)
-- ============================================================================
CREATE TABLE IF NOT EXISTS disputes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispute_code VARCHAR(50) NOT NULL UNIQUE,
    transaction_id BIGINT NOT NULL,
    raised_by VARCHAR(255) NOT NULL,
    description TEXT,
    admin_note TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_disputes_status (status),
    INDEX idx_disputes_transaction (transaction_id),
    INDEX idx_disputes_raised_by (raised_by),
    INDEX idx_disputes_created_at (created_at),
    INDEX idx_disputes_code (dispute_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Disputes/complaints raised by Buyers or EV Owners';

-- ============================================================================
-- 9. SETTINGS TABLE (No dependencies)
-- ============================================================================
CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL UNIQUE,
    value VARCHAR(500) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_settings_key_name (key_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='System-wide configuration settings managed by admin';

-- Seed default settings (idempotent)
INSERT INTO settings (key_name, value, description, updated_at)
SELECT * FROM (
    SELECT 'carbon_credit_conversion_rate', '15.0',  'Conversion rate: 1 Carbon Credit = X USD', NOW() UNION ALL
    SELECT 'carbon_credit_min_price',       '10.0',  'Minimum price per carbon credit (USD)',   NOW() UNION ALL
    SELECT 'carbon_credit_max_price',       '100.0', 'Maximum price per carbon credit (USD)',   NOW() UNION ALL
    SELECT 'transaction_fee_percentage',    '2.5',   'Transaction fee percentage (e.g., 2.5%)', NOW() UNION ALL
    SELECT 'transaction_fee_fixed',         '1.0',   'Fixed transaction fee (USD)',             NOW() UNION ALL
    SELECT 'max_listing_per_user',          '50',    'Maximum number of listings per user',     NOW() UNION ALL
    SELECT 'max_transaction_per_day',       '20',    'Maximum transactions per user per day',   NOW() UNION ALL
    SELECT 'max_dispute_per_user',          '10',    'Maximum disputes per user',               NOW() UNION ALL
    SELECT 'max_upload_size_mb',            '10',    'Maximum file upload size in MB',          NOW() UNION ALL
    SELECT 'session_timeout_minutes',       '60',    'User session timeout in minutes',         NOW() UNION ALL
    SELECT 'email_notifications_enabled',   'true',  'Enable email notifications',               NOW() UNION ALL
    SELECT 'sms_notifications_enabled',     'false', 'Enable SMS notifications',                 NOW() UNION ALL
    SELECT 'maintenance_mode',              'false', 'Enable maintenance mode (blocks non-admin access)', NOW() UNION ALL
    SELECT 'maintenance_message',           'System under maintenance', 'Message shown during maintenance', NOW()
) AS s
WHERE NOT EXISTS (SELECT 1 FROM settings LIMIT 1);

-- ============================================================================
-- 10. REFRESH_TOKENS TABLE (Depends on: users)
-- ============================================================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE COMMENT 'Unique refresh token string (UUID or JWT)',
    user_id BIGINT NOT NULL COMMENT 'Associated user ID',
    expires_at TIMESTAMP NOT NULL COMMENT 'Token expiration timestamp',
    revoked BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Token revocation flag (logout or security)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Token creation timestamp',

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_token (token),
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at),
    INDEX idx_revoked (revoked),
    INDEX idx_user_active (user_id, revoked, expires_at) COMMENT 'Composite index for active token lookup'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Refresh tokens for JWT authentication';

-- ============================================================================
-- 11. COMPOSITE INDEXES FOR QUERY OPTIMIZATION (2025-11-06)
-- ============================================================================
-- Note: Using procedure for idempotent index creation (compatible with MySQL 5.7+)

DELIMITER $$

-- Helper procedure to create index only if it doesn't exist
CREATE PROCEDURE CreateIndexIfNotExists(
    IN indexName VARCHAR(128),
    IN tableName VARCHAR(128),
    IN indexDefinition TEXT
)
BEGIN
    DECLARE indexExists INT DEFAULT 0;
    
    -- Check if index exists
    SELECT COUNT(*) INTO indexExists
    FROM information_schema.STATISTICS
    WHERE table_schema = DATABASE()
      AND table_name = tableName
      AND index_name = indexName;
    
    -- Create index if not exists
    IF indexExists = 0 THEN
        SET @sql = CONCAT('CREATE INDEX ', indexName, ' ON ', tableName, ' ', indexDefinition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$

DELIMITER ;

-- TRANSACTIONS
CALL CreateIndexIfNotExists('idx_transactions_status_created_at', 'transactions', '(status, created_at DESC)');
CALL CreateIndexIfNotExists('idx_transactions_type_created_at', 'transactions', '(type, created_at DESC)');

-- USERS
CALL CreateIndexIfNotExists('idx_users_role_status_created_at', 'users', '(role_id, status, created_at DESC)');

-- DISPUTES
CALL CreateIndexIfNotExists('idx_disputes_status_created_at', 'disputes', '(status, created_at DESC)');

-- LISTINGS
CALL CreateIndexIfNotExists('idx_listings_status_created_at', 'listings', '(status, created_at DESC)');
CALL CreateIndexIfNotExists('idx_listings_owner_status_created_at', 'listings', '(owner_id, status, created_at DESC)');

-- Cleanup: Drop the helper procedure
DROP PROCEDURE IF EXISTS CreateIndexIfNotExists;

