ALTER TABLE sales_workflows
ADD COLUMN IF NOT EXISTS region_id BIGINT;

ALTER TABLE sales_workflows
ALTER COLUMN region DROP NOT NULL;

ALTER TABLE sales_workflows
ADD CONSTRAINT fk_sales_workflows_region
FOREIGN KEY (region_id)
REFERENCES regions(id);