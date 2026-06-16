CREATE TABLE contracts (
    id BIGSERIAL PRIMARY KEY,
    contract_number VARCHAR(50) NOT NULL UNIQUE,
    offer_id BIGINT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    sales_process_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_value NUMERIC(12, 2) NOT NULL,
    terms VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    signed_at TIMESTAMP,

    CONSTRAINT fk_contracts_offer
        FOREIGN KEY (offer_id)
        REFERENCES offers(id),

    CONSTRAINT fk_contracts_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_contracts_sales_process
        FOREIGN KEY (sales_process_id)
        REFERENCES sales_processes(id)
);