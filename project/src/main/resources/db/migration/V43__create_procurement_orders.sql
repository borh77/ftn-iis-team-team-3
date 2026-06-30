CREATE TABLE procurement_orders (
    id BIGSERIAL PRIMARY KEY,
    buyer_id BIGINT NOT NULL REFERENCES users(id),
    buyer_username VARCHAR(100) NOT NULL,
    buyer_display_name VARCHAR(255),
    region_id BIGINT REFERENCES regions(id),
    region_name VARCHAR(255),
    customer_segment VARCHAR(100) NOT NULL,
    pricelist_id BIGINT REFERENCES pricelists(id),
    source_file_name VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    total_price NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10),
    created_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP NOT NULL
);

CREATE TABLE procurement_order_items (
    id BIGSERIAL PRIMARY KEY,
    procurement_order_id BIGINT NOT NULL REFERENCES procurement_orders(id) ON DELETE CASCADE,
    original_variant_id BIGINT NULL,
    original_variant_name VARCHAR(255) NULL,
    variant_id BIGINT NOT NULL,
    variant_name VARCHAR(255) NOT NULL,
    requested_quantity INTEGER NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    discount_type VARCHAR(30) NULL,
    discount_value NUMERIC(12,2) NULL,
    final_unit_price NUMERIC(12,2) NOT NULL,
    line_total NUMERIC(12,2) NOT NULL,
    replacement_accepted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_procurement_orders_buyer_created_at
    ON procurement_orders (buyer_id, created_at DESC);

CREATE INDEX idx_procurement_order_items_order
    ON procurement_order_items (procurement_order_id);
