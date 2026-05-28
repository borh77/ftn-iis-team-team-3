-- Add created_by column to pricelists to track creator user id
ALTER TABLE pricelists
    ADD COLUMN created_by BIGINT;

-- Optionally add foreign key to users(id) if desired:
-- ALTER TABLE pricelists ADD CONSTRAINT fk_pricelists_created_by FOREIGN KEY (created_by) REFERENCES users(id);
