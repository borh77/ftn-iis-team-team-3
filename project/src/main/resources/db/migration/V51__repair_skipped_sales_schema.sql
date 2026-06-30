ALTER TABLE sales_process_history
ADD COLUMN IF NOT EXISTS changed_by_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_sales_process_history_changed_by'
          AND conrelid = 'sales_process_history'::regclass
    ) THEN
        ALTER TABLE sales_process_history
        ADD CONSTRAINT fk_sales_process_history_changed_by
        FOREIGN KEY (changed_by_id)
        REFERENCES users(id);
    END IF;
END $$;

ALTER TABLE customers
ADD COLUMN IF NOT EXISTS region_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_customers_region'
          AND conrelid = 'customers'::regclass
    ) THEN
        ALTER TABLE customers
        ADD CONSTRAINT fk_customers_region
        FOREIGN KEY (region_id)
        REFERENCES regions(id);
    END IF;
END $$;

UPDATE customers
SET region_id = (SELECT id FROM regions ORDER BY id LIMIT 1)
WHERE region_id IS NULL;

ALTER TABLE leads
ADD COLUMN IF NOT EXISTS region_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_leads_region'
          AND conrelid = 'leads'::regclass
    ) THEN
        ALTER TABLE leads
        ADD CONSTRAINT fk_leads_region
        FOREIGN KEY (region_id)
        REFERENCES regions(id);
    END IF;
END $$;

ALTER TABLE sales_processes
ADD COLUMN IF NOT EXISTS workflow_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_sales_processes_workflow'
          AND conrelid = 'sales_processes'::regclass
    ) THEN
        ALTER TABLE sales_processes
        ADD CONSTRAINT fk_sales_processes_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES sales_workflows(id);
    END IF;
END $$;
