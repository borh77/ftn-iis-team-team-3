CREATE TABLE sales_activities (
    id BIGSERIAL PRIMARY KEY,
    sales_process_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    scheduled_at TIMESTAMP,
    completed_at TIMESTAMP,

    CONSTRAINT fk_activity_process
        FOREIGN KEY (sales_process_id)
        REFERENCES sales_processes(id)
);