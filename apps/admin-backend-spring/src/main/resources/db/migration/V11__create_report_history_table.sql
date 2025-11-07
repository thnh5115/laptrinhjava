-- Migration V11: Create report_history table for audit trail
-- Track all report generations by admins

CREATE TABLE IF NOT EXISTS report_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(50) NOT NULL COMMENT 'Report type: TRANSACTIONS, USERS',
    generated_by BIGINT NOT NULL COMMENT 'Admin who generated the report',
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date DATE COMMENT 'Report date range start',
    end_date DATE COMMENT 'Report date range end',
    format VARCHAR(20) NOT NULL COMMENT 'Export format: CSV, PDF, EXCEL',
    file_path VARCHAR(500) COMMENT 'Path to generated file (if stored)',
    parameters TEXT COMMENT 'JSON of filter parameters used',
    
    -- Foreign key constraint
    CONSTRAINT fk_report_history_admin 
        FOREIGN KEY (generated_by) 
        REFERENCES users(id) 
        ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_report_history_generated_by (generated_by),
    INDEX idx_report_history_generated_at (generated_at DESC),
    INDEX idx_report_history_type (type),
    INDEX idx_report_history_composite (type, generated_by, generated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Audit trail of report generations by admins';

-- Sample report history data
INSERT INTO report_history (type, generated_by, generated_at, start_date, end_date, format, parameters) VALUES
('TRANSACTIONS', 1, '2025-01-15 09:30:00', '2024-01-01', '2024-12-31', 'CSV', '{"status":"COMPLETED","type":"PURCHASE"}'),
('TRANSACTIONS', 1, '2025-01-15 10:15:00', '2024-01-01', '2024-12-31', 'PDF', '{"status":"COMPLETED"}'),
('TRANSACTIONS', 1, '2025-01-16 14:20:00', '2025-01-01', '2025-01-15', 'EXCEL', '{"keyword":"carbon"}'),
('USERS', 1, '2025-01-17 11:00:00', NULL, NULL, 'CSV', '{"role":"BUYER","status":"ACTIVE"}'),
('USERS', 1, '2025-01-17 11:30:00', NULL, NULL, 'PDF', '{"role":"ADMIN"}'),
('TRANSACTIONS', 1, '2025-01-18 16:45:00', '2024-06-01', '2024-12-31', 'CSV', '{"status":"PENDING"}');
