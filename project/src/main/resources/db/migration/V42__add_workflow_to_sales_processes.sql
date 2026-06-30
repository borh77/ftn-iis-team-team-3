ALTER TABLE sales_processes
ADD COLUMN IF NOT EXISTS workflow_id BIGINT;

ALTER TABLE sales_processes
ADD CONSTRAINT fk_sales_processes_workflow
FOREIGN KEY (workflow_id)
REFERENCES sales_workflows(id);