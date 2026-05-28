-- Add created_by column to pricelists to track creator user id
ALTER TABLE pricelists
    ADD COLUMN created_by BIGINT;

