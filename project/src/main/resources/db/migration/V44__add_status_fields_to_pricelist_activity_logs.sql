ALTER TABLE pricelist_activity_logs
    ADD COLUMN IF NOT EXISTS status_from VARCHAR(32),
    ADD COLUMN IF NOT EXISTS status_to VARCHAR(32);

CREATE INDEX IF NOT EXISTS idx_pricelist_activity_logs_action_status_to_timestamp_desc
    ON pricelist_activity_logs(action_type, status_to, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_pricelist_activity_logs_pricelist_action_timestamp
    ON pricelist_activity_logs(pricelist_id, action_type, timestamp);
