CREATE TABLE offers (
    id BIGSERIAL PRIMARY KEY,
    offer_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    sales_process_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    valid_until DATE NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_offers_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_offers_sales_process
        FOREIGN KEY (sales_process_id)
        REFERENCES sales_processes(id)
);

CREATE TABLE offer_items (
    id BIGSERIAL PRIMARY KEY,
    offer_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    total_price NUMERIC(12, 2) NOT NULL,

    CONSTRAINT fk_offer_items_offer
        FOREIGN KEY (offer_id)
        REFERENCES offers(id),

    CONSTRAINT fk_offer_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
);