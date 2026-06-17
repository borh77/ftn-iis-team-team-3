CREATE TABLE customer_needs (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    sales_process_id BIGINT NOT NULL,
    description VARCHAR(1000) NOT NULL,
    priority VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_customer_needs_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(id),

    CONSTRAINT fk_customer_needs_sales_process
        FOREIGN KEY (sales_process_id)
        REFERENCES sales_processes(id)
);