CREATE TABLE IF NOT EXISTS pricelist_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    pricelist_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    team_id BIGINT,
    action_type VARCHAR(64) NOT NULL,
    description VARCHAR(500) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_pricelist_activity_logs_pricelist'
          AND conrelid = 'pricelist_activity_logs'::regclass
    ) THEN
        ALTER TABLE pricelist_activity_logs
            ADD CONSTRAINT fk_pricelist_activity_logs_pricelist
            FOREIGN KEY (pricelist_id)
            REFERENCES pricelists(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_pricelist_activity_logs_user'
          AND conrelid = 'pricelist_activity_logs'::regclass
    ) THEN
        ALTER TABLE pricelist_activity_logs
            ADD CONSTRAINT fk_pricelist_activity_logs_user
            FOREIGN KEY (user_id)
            REFERENCES users(id);
    END IF;

    IF to_regclass('pricelist_teams') IS NOT NULL
       AND NOT EXISTS (
           SELECT 1
           FROM pg_constraint
           WHERE conname = 'fk_pricelist_activity_logs_team'
             AND conrelid = 'pricelist_activity_logs'::regclass
       ) THEN
        ALTER TABLE pricelist_activity_logs
            ADD CONSTRAINT fk_pricelist_activity_logs_team
            FOREIGN KEY (team_id)
            REFERENCES pricelist_teams(id);
    END IF;
END;
$$;

CREATE INDEX IF NOT EXISTS idx_pricelist_activity_logs_timestamp_desc
    ON pricelist_activity_logs(timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_pricelist_activity_logs_team_timestamp_desc
    ON pricelist_activity_logs(team_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_pricelist_activity_logs_user_timestamp_desc
    ON pricelist_activity_logs(user_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_pricelist_activity_logs_pricelist_timestamp_desc
    ON pricelist_activity_logs(pricelist_id, timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_pricelist_activity_logs_action_timestamp_desc
    ON pricelist_activity_logs(action_type, timestamp DESC);
