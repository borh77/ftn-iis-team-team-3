ALTER TABLE customers
ADD COLUMN IF NOT EXISTS region_id BIGINT;

ALTER TABLE customers
ADD CONSTRAINT fk_customers_region
FOREIGN KEY (region_id)
REFERENCES regions(id);

UPDATE customers
SET region_id = (SELECT id FROM regions ORDER BY id LIMIT 1)
WHERE region_id IS NULL;