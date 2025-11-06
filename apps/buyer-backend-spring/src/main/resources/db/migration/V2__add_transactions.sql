CREATE TABLE IF NOT EXISTS transactions (
id BIGINT PRIMARY KEY AUTO_INCREMENT,
order_id BIGINT NOT NULL,
amount DECIMAL(18,2) NOT NULL,
transaction_ref VARCHAR(100) NOT NULL UNIQUE,
status VARCHAR(30) NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
CONSTRAINT fk_transactions_order
FOREIGN KEY (order_id) REFERENCES credit_orders(id)
ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Payment transactions for orders';

ALTER TABLE credit_orders
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NULL DEFAULT NULL AFTER created_at;

CREATE INDEX IF NOT EXISTS idx_transactions_order ON transactions(order_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at);