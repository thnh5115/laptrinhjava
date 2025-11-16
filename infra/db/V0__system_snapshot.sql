SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- MODULE 1: QUẢN LÝ NGƯỜI DÙNG & VAI TRÒ (Giải quyết Xung đột #1)
-- (Hợp nhất `users`, `buyers`, `ev_owners` vào một bảng `users` duy nhất)
-- ============================================================================

-- Bảng 1: roles (Bảng Vai trò)
-- (Giữ nguyên từ ADMIN)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng "master" quản lý Vai trò (ADMIN, AUDITOR, BUYER, EV_OWNER)';

-- Chèn mồi (seed) 4 vai trò mặc định
INSERT INTO roles (name, description)
SELECT * FROM (
    SELECT 'ADMIN',    'Quản trị hệ thống' UNION ALL
    SELECT 'AUDITOR',  'Người xác minh (CVA)' UNION ALL
    SELECT 'BUYER',    'Người mua tín chỉ' UNION ALL
    SELECT 'EV_OWNER', 'Chủ sở hữu xe điện (Người bán)'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM roles LIMIT 1);


-- Bảng 2: users (Bảng Người dùng Master)
-- (Bảng trung tâm, thay thế `buyers` và `ev_owners`)
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
    INDEX idx_users_email_lower ((LOWER(email)))

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng "master" quản lý TẤT CẢ người dùng';


-- ============================================================================
-- MODULE 2: TÀI CHÍNH & VÍ (Giải quyết Xung đột #2)
-- (Tạo ví tiền và quản lý dòng tiền ra)
-- ============================================================================

-- Bảng 3: e_wallets (Ví Tiền)
-- (Bảng MỚI, thay thế cột `buyers.balance`)
CREATE TABLE IF NOT EXISTS e_wallets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT 'Liên kết 1-1 tới bảng users (chủ sở hữu ví)',
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00 COMMENT 'Số dư tiền tệ (ví dụ: USD)',
    currency VARCHAR(10) NOT NULL DEFAULT 'USD' COMMENT 'Loại tiền tệ (USD, VND)',
    status ENUM('ACTIVE', 'FROZEN') NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái của ví',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_ewallet_user FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Ví tiền (Money Wallet) của người dùng (Buyer, EV Owner)';


-- Bảng 4: payouts (Yêu cầu Rút tiền)
-- (Giữ nguyên từ ADMIN V9, liên kết với `users`)
CREATE TABLE IF NOT EXISTS payouts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'Người yêu cầu rút tiền (từ bảng users)',
    e_wallet_id BIGINT NOT NULL COMMENT 'Rút từ ví tiền nào (từ bảng e_wallets)',
    amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền yêu cầu rút',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, APPROVED, REJECTED, COMPLETED',
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP NULL,
    processed_by BIGINT NULL COMMENT 'Admin xử lý (từ bảng users)',
    notes TEXT COMMENT 'Ghi chú của Admin (lý do từ chối)',
    bank_account VARCHAR(255) COMMENT 'Thông tin tài khoản nhận tiền',
    payment_method VARCHAR(50) NULL COMMENT 'Phuong thuc chi tra (BANK_TRANSFER, PAYPAL, ...)',


    CONSTRAINT fk_payout_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_payout_wallet FOREIGN KEY (e_wallet_id) REFERENCES e_wallets(id),
    CONSTRAINT fk_payout_processor FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_payout_user_status (user_id, status),
    INDEX idx_payout_status (status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Quản lý các yêu cầu RÚT TIỀN (dòng tiền ra)';


-- Bảng 5: deposits (Lịch sử Nạp tiền)
-- (Bảng MỚI, cần thiết để quản lý dòng tiền vào)
CREATE TABLE IF NOT EXISTS deposits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'Người nạp tiền (từ bảng users)',
    e_wallet_id BIGINT NOT NULL COMMENT 'Nạp vào ví tiền nào (từ bảng e_wallets)',
    amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền nạp',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, SUCCESS, FAILED',
    payment_gateway_ref VARCHAR(100) UNIQUE COMMENT 'Mã tham chiếu của Cổng thanh toán (Momo, Stripe...)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_deposit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_deposit_wallet FOREIGN KEY (e_wallet_id) REFERENCES e_wallets(id),

    INDEX idx_deposit_user (user_id),
    INDEX idx_deposit_status (status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Quản lý các giao dịch NẠP TIỀN (dòng tiền vào)';


-- ============================================================================
-- MODULE 3: HÀNH TRÌNH & TÍN CHỈ (Giải quyết Xung đột #3 & #4)
-- (Hợp nhất 3 bảng `journeys` và 3 bảng `carbon_credits`)
-- ============================================================================

-- Bảng 6: journeys (Hành trình)
-- (Bảng "master" thay thế `verification_requests` và `journeys` của EV_OWNER)
CREATE TABLE IF NOT EXISTS journeys (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'ID của EV Owner (từ bảng users)',
    journey_date DATE NOT NULL COMMENT 'Ngày diễn ra chuyến đi',
    start_location VARCHAR(255) COMMENT 'Dia diem bat dau hanh trinh',
    end_location VARCHAR(255) COMMENT 'Dia diem ket thuc hanh trinh',
    distance_km DECIMAL(10,2) NOT NULL COMMENT 'Sửa từ DOUBLE thành DECIMAL',
    energy_used_kwh DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, VERIFIED, REJECTED',
    verified_by BIGINT NULL COMMENT 'ID của CVA/Auditor (từ bảng users)',
    verified_at TIMESTAMP NULL COMMENT 'Thời điểm xác minh',
    rejection_reason TEXT,
    credits_generated DECIMAL(10,2) COMMENT 'Số tín chỉ được tạo ra (nếu VERIFIED)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_journey_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_journey_verifier FOREIGN KEY (verified_by) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_journey_user_status (user_id, status),
    INDEX idx_journey_status (status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Quản lý toàn bộ vòng đời chuyến đi (nộp, chờ duyệt, đã duyệt)';


-- Bảng 7: carbon_credits (Sổ cái Tín chỉ Carbon)
-- (Bảng "master" thay thế 2 bảng `carbon_credits` khác và bảng `carbon_wallets`)
CREATE TABLE IF NOT EXISTS carbon_credits (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    owner_id BIGINT NOT NULL COMMENT 'Chủ sở hữu tín chỉ (từ bảng users)',
    journey_id BIGINT NOT NULL UNIQUE COMMENT 'Chuyến đi đã tạo ra tín chỉ này (từ bảng journeys)',
    amount DECIMAL(10,2) NOT NULL COMMENT 'Số lượng tín chỉ (ví dụ: tons CO2)',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'Trạng thái: AVAILABLE, LISTED, SOLD, RESERVED, RETIRED',
    price_per_credit DECIMAL(10,2) COMMENT 'Giá khi được niêm yết',
    listed_at TIMESTAMP NULL,
    sold_at TIMESTAMP NULL,
    buyer_id BIGINT NULL COMMENT 'Người mua tín chỉ (từ bảng users)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_credit_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_journey FOREIGN KEY (journey_id) REFERENCES journeys(id) ON DELETE CASCADE,
    CONSTRAINT fk_credit_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE SET NULL,

    INDEX idx_credit_owner_status (owner_id, status),
    INDEX idx_credit_status (status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng "master" (sổ cái) quản lý tất cả tín chỉ carbon';


-- ============================================================================
-- MODULE 4: SÀN GIAO DỊCH (Giải quyết Xung đột #5)
-- (Hợp nhất 3 bảng `listings` và bổ sung logic Đấu giá)
-- ============================================================================

-- Bảng 8: listings (Niêm yết Bán hàng)
-- (Bảng "master" hợp nhất từ 3 bảng `listings`/`credit_listings`)
CREATE TABLE IF NOT EXISTS listings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    carbon_credit_id BIGINT NOT NULL UNIQUE COMMENT 'Lo tan cho duoc dem ban (tu bang carbon_credits)',
    seller_id BIGINT NOT NULL COMMENT 'Nguoi ban (tu bang users)',
    title VARCHAR(255) NOT NULL COMMENT 'Ten listing hien thi',
    description TEXT COMMENT 'Mo ta chi tiet',
    listing_type ENUM('FIXED_PRICE', 'AUCTION') NOT NULL DEFAULT 'FIXED_PRICE',
    price DECIMAL(15, 2) NOT NULL COMMENT 'Gia (cho FIXED_PRICE)',
    quantity DECIMAL(10, 2) NOT NULL COMMENT 'So luong tan cho trong lo nay',
    unit VARCHAR(50) NULL COMMENT 'Don vi do luong (kgCO2, tCO2, ...)',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, ACTIVE, SOLD, CANCELLED',
    approved_by BIGINT NULL COMMENT 'Admin phe duyet/go',
    approved_at TIMESTAMP NULL COMMENT 'Thoi diem phe duyet/go',
    reject_reason TEXT COMMENT 'Ly do bi tu choi/go',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_listing_credit FOREIGN KEY (carbon_credit_id) REFERENCES carbon_credits(id),
    CONSTRAINT fk_listing_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_listing_approved_by FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_listings_seller_id (seller_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bang "master" quan ly tat ca cac niem yet ban';


-- Bảng 9: auctions (Phiên Đấu giá)
-- (Bổ sung từ gói BUYER để hỗ trợ `listing_type = 'AUCTION'`)
CREATE TABLE IF NOT EXISTS auctions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    listing_id BIGINT NOT NULL UNIQUE COMMENT 'Liên kết 1-1 tới niêm yết',
    start_price DECIMAL(15, 2) NOT NULL COMMENT 'Giá khởi điểm',
    step_price DECIMAL(15, 2) NOT NULL DEFAULT 1.00 COMMENT 'Bước giá',
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_auction_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Chi tiết các phiên đấu giá (nếu listing_type = AUCTION)';


-- Bảng 10: bids (Lượt Đấu giá)
-- (Bổ sung từ gói BUYER, liên kết với `users` và `auctions`)
CREATE TABLE IF NOT EXISTS bids (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    auction_id BIGINT NOT NULL COMMENT 'Phiên đấu giá (từ bảng auctions)',
    buyer_id BIGINT NOT NULL COMMENT 'Người đặt giá (từ bảng users)',
    bid_price DECIMAL(15, 2) NOT NULL COMMENT 'Giá đặt',
    status VARCHAR(20) NOT NULL DEFAULT 'LEADING' COMMENT 'LEADING, OUTBID, WON, LOST',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_bid_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    CONSTRAINT fk_bid_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_bids_auction_buyer (auction_id, buyer_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Lịch sử các lượt đặt giá của Buyer cho phiên đấu giá';


-- ============================================================================
-- MODULE 5: GIAO DỊCH & THANH TOÁN (Giải quyết Xung đột #6)
-- (Hợp nhất 3 bảng `transactions` và `carbon_credit_transactions`)
-- ============================================================================

-- Bảng 11: transactions (Giao dịch Mua bán Tiền tệ)
-- (Bảng "master" thay thế 2 bảng `transactions` khác)
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_code VARCHAR(100) UNIQUE COMMENT 'Admin-friendly transaction code',
    buyer_id BIGINT NOT NULL COMMENT 'Buyer (from users table)',
    buyer_email VARCHAR(255) COMMENT 'Snapshot of buyer email for quick lookup',
    seller_email VARCHAR(255) COMMENT 'Snapshot of seller email',
    listing_id BIGINT NOT NULL COMMENT 'Listing purchased (from listings table)',
    quantity DECIMAL(10, 2) NOT NULL COMMENT 'Quantity of credits purchased',
    total_amount DECIMAL(15, 2) NOT NULL COMMENT 'Total monetary amount',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PAID, COMPLETED, FAILED, REFUNDED',
    type VARCHAR(20) NOT NULL DEFAULT 'CREDIT_PURCHASE' COMMENT 'Transaction type enum',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tx_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_tx_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
    UNIQUE KEY uk_transactions_code (transaction_code),
    INDEX idx_tx_buyer_id (buyer_id),
    INDEX idx_tx_status_created (status, created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Master table for monetary transactions (buy/sell)';


-- Bảng 12: payments (Chi tiết Thanh toán)
-- (Bổ sung từ gói BUYER, liên kết với `transactions`)

CREATE TABLE IF NOT EXISTS transaction_audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL COMMENT 'Transaction being audited',
    transaction_code VARCHAR(100) NOT NULL COMMENT 'Transaction code for referencing',
    old_status VARCHAR(20) NOT NULL COMMENT 'Previous status',
    new_status VARCHAR(20) NOT NULL COMMENT 'New status after update',
    changed_by VARCHAR(255) NOT NULL COMMENT 'Admin who performed the change',
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Timestamp of change',
    reason VARCHAR(500) COMMENT 'Optional explanation',

    CONSTRAINT fk_tx_audit_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    INDEX idx_tx_audit_transaction (transaction_id),
    INDEX idx_tx_audit_changed_at (changed_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='History of transaction status changes for auditing';

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL UNIQUE COMMENT 'Giao dịch (từ bảng transactions)',
    method VARCHAR(50) NOT NULL COMMENT 'Phương thức TT (ví dụ: E_WALLET, STRIPE)',
    status VARCHAR(20) NOT NULL COMMENT 'SUCCESS, FAILED',
    payment_gateway_ref VARCHAR(100) UNIQUE,
    amount DECIMAL(15, 2) NOT NULL COMMENT 'Số tiền đã thanh toán',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_payment_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Chi tiết một lần thanh toán (cho 1 transaction)';


-- Bảng 13: invoices (Hóa đơn)
-- (Bổ sung từ gói BUYER, liên kết với `transactions`)
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL UNIQUE COMMENT 'Giao dịch (từ bảng transactions)',
    issue_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    file_path VARCHAR(255) NOT NULL,
    
    CONSTRAINT fk_invoice_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Hóa đơn được tạo ra sau khi thanh toán';


-- ============================================================================
-- MODULE 6: HỆ THỐNG & ADMIN (Các bảng còn lại của ADMIN)
-- ============================================================================

-- Bảng 14: disputes (Tranh chấp)
-- (Giữ nguyên từ ADMIN, liên kết `transaction_id` với `transactions` master)
CREATE TABLE IF NOT EXISTS disputes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dispute_code VARCHAR(50) NOT NULL UNIQUE,
    transaction_id BIGINT NOT NULL COMMENT 'Giao dịch bị tranh chấp (từ bảng transactions)',
    raised_by_user_id BIGINT NOT NULL COMMENT 'Người nêu tranh chấp (từ bảng users)',
    description TEXT,
    admin_note TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN, IN_REVIEW, RESOLVED, REJECTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_dispute_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_dispute_user FOREIGN KEY (raised_by_user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_disputes_status (status),
    INDEX idx_disputes_transaction (transaction_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Quản lý tranh chấp liên quan đến giao dịch';


-- Bảng 15: notifications (Thông báo)
-- (Bổ sung từ gói BUYER, nhưng liên kết với `users` master)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT 'Người nhận (từ bảng users)',
    message VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME NULL,
    
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_notifications_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Thông báo cho người dùng (đấu giá, giao dịch...)';


-- Bảng 16: settings (Cài đặt hệ thống)
-- (Giữ nguyên từ ADMIN)
CREATE TABLE IF NOT EXISTS settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(100) NOT NULL UNIQUE,
    value VARCHAR(500) NOT NULL,
    description TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Cài đặt toàn hệ thống (key-value)';


-- Bảng 17: audit_logs (Nhật ký Nghiệp vụ)
-- (Giữ nguyên từ ADMIN, liên kết với `users`)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_id BIGINT NULL COMMENT 'Người thực hiện (từ bảng users)',
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
COMMENT='Nhật ký nghiệp vụ (Ai làm gì, khi nào)';


-- Bảng 18: http_audit_logs (Nhật ký HTTP)
-- (Giữ nguyên từ ADMIN)
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
COMMENT='Nhật ký kỹ thuật (HTTP request logs)';


-- Bảng 19: refresh_tokens (Token Xác thực)
-- (Giữ nguyên từ ADMIN, liên kết với `users`)
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_token (token),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Refresh tokens cho việc xác thực (authentication)';


-- Bảng 20: report_history (Lịch sử Báo cáo)
-- (Giữ nguyên từ ADMIN, liên kết với `users`)
CREATE TABLE IF NOT EXISTS report_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL,
    generated_by BIGINT NOT NULL COMMENT 'Admin (từ bảng users)',
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date DATE,
    end_date DATE,
    format VARCHAR(20) NOT NULL,
    file_path VARCHAR(500),
    parameters TEXT,
    
    CONSTRAINT fk_report_history_admin 
        FOREIGN KEY (generated_by) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    INDEX idx_report_history_generated_by (generated_by),
    INDEX idx_report_history_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Lịch sử các báo cáo do Admin tạo ra';


SET FOREIGN_KEY_CHECKS = 1;
-- ======================== KẾT THÚC LƯỢC ĐỒ THỐNG NHẤT ========================