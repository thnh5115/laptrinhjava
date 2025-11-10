CREATE TABLE ev_owners (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE wallets (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         total_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
                         locked_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
                         owner_id BIGINT NOT NULL UNIQUE,
                         CONSTRAINT fk_wallet_owner FOREIGN KEY (owner_id) REFERENCES ev_owners(id)
);

CREATE TABLE journeys (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          owner_id BIGINT NOT NULL,
                          distance_km DOUBLE NOT NULL,
                          co2_reduced_kg DOUBLE NULL,
                          CONSTRAINT fk_journey_owner FOREIGN KEY (owner_id) REFERENCES ev_owners(id)
);

CREATE TABLE carbon_credits (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                wallet_id BIGINT NOT NULL,
                                source_journey_id BIGINT NULL,
                                amount DECIMAL(19, 4) NOT NULL,
                                status VARCHAR(50) NOT NULL,
                                created_at TIMESTAMP NOT NULL,

                                CONSTRAINT fk_credit_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),
                                CONSTRAINT fk_credit_journey FOREIGN KEY (source_journey_id) REFERENCES journeys(id)
);

CREATE TABLE carbon_credit_transactions (
                                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            carbon_credit_id BIGINT NOT NULL,
                                            source_wallet_id BIGINT NULL,
                                            destination_wallet_id BIGINT NULL,
                                            transaction_type VARCHAR(50) NOT NULL,
                                            amount DECIMAL(19, 4) NOT NULL,
                                            timestamp TIMESTAMP NOT NULL,

                                            CONSTRAINT fk_tx_credit FOREIGN KEY (carbon_credit_id) REFERENCES carbon_credits(id),
                                            CONSTRAINT fk_tx_source_wallet FOREIGN KEY (source_wallet_id) REFERENCES wallets(id),
                                            CONSTRAINT fk_tx_dest_wallet FOREIGN KEY (destination_wallet_id) REFERENCES wallets(id)
);

-- Add indexes for better query performance
CREATE INDEX idx_credits_wallet_status ON carbon_credits(wallet_id, status);
CREATE INDEX idx_tx_source_wallet ON carbon_credit_transactions(source_wallet_id);
CREATE INDEX idx_tx_dest_wallet ON carbon_credit_transactions(destination_wallet_id);