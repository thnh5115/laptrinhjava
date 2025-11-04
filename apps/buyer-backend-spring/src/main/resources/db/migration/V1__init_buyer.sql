CREATE TABLE buyers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    organization VARCHAR(255),
    created_at DATETIME NOT NULL
);

CREATE TABLE credit_orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    buyer_id BIGINT NOT NULL,
    amount DOUBLE NOT NULL,
    price DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_credit_orders_buyer FOREIGN KEY (buyer_id) REFERENCES buyers(id)
);
