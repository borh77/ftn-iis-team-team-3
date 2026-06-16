CREATE TABLE sales_process_history (
    id BIGSERIAL PRIMARY KEY,
    sales_process_id BIGINT NOT NULL,
    previous_stage VARCHAR(50) NOT NULL,
    new_stage VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sales_process_history_process
        FOREIGN KEY (sales_process_id)
        REFERENCES sales_processes(id)
);