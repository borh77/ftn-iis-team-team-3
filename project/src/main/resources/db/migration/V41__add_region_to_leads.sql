ALTER TABLE leads
ADD COLUMN IF NOT EXISTS region_id BIGINT;

ALTER TABLE leads
ADD CONSTRAINT fk_leads_region
FOREIGN KEY (region_id)
REFERENCES regions(id);