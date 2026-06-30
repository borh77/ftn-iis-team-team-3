ALTER TABLE sales_process_history
ADD COLUMN IF NOT EXISTS changed_by_id BIGINT;

ALTER TABLE sales_process_history
ADD CONSTRAINT fk_sales_process_history_changed_by
FOREIGN KEY (changed_by_id)
REFERENCES users(id);