ALTER TABLE users
    ADD COLUMN buyer_region_id BIGINT,
    ADD COLUMN customer_segment VARCHAR(120);

ALTER TABLE users
    ADD CONSTRAINT fk_users_buyer_region
        FOREIGN KEY (buyer_region_id)
        REFERENCES regions(id)
        ON DELETE SET NULL;

CREATE INDEX idx_users_buyer_region_id ON users(buyer_region_id);
