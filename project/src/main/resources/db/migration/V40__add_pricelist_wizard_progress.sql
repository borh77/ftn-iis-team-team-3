ALTER TABLE pricelists
    ADD COLUMN IF NOT EXISTS creation_step VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    ADD COLUMN IF NOT EXISTS creation_completed BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS last_edited_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS team_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_pricelists_team'
    ) THEN
        ALTER TABLE pricelists
            ADD CONSTRAINT fk_pricelists_team
                FOREIGN KEY (team_id)
                REFERENCES pricelist_teams(id)
                ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_pricelists_created_by_creation_completed
    ON pricelists(created_by, creation_completed);

CREATE INDEX IF NOT EXISTS idx_pricelists_team_id
    ON pricelists(team_id);
