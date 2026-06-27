ALTER TABLE pricelists
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS status_changed_at TIMESTAMPTZ;

UPDATE pricelists
SET created_at = CURRENT_TIMESTAMP
WHERE created_at IS NULL;

UPDATE pricelists
SET status_changed_at = COALESCE(status_changed_at, created_at, CURRENT_TIMESTAMP)
WHERE status_changed_at IS NULL;

ALTER TABLE pricelists
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN status_changed_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN status_changed_at SET NOT NULL;

CREATE TABLE IF NOT EXISTS pricelist_status_history (
    id BIGSERIAL PRIMARY KEY,
    pricelist_id BIGINT NOT NULL,
    old_status VARCHAR(32) NOT NULL,
    new_status VARCHAR(32) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL,
    duration_in_previous_status_seconds BIGINT NOT NULL
);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_pricelist_status_history_pricelist'
          AND conrelid = 'pricelist_status_history'::regclass
    ) THEN
        ALTER TABLE pricelist_status_history
            ADD CONSTRAINT fk_pricelist_status_history_pricelist
            FOREIGN KEY (pricelist_id)
            REFERENCES pricelists(id)
            ON DELETE CASCADE;
    END IF;
END;
$$;

CREATE INDEX IF NOT EXISTS idx_pricelist_status_history_pricelist_id
    ON pricelist_status_history(pricelist_id);

CREATE INDEX IF NOT EXISTS idx_pricelist_status_history_changed_at_desc
    ON pricelist_status_history(changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_pricelist_status_history_new_status_changed_at_desc
    ON pricelist_status_history(new_status, changed_at DESC);

CREATE OR REPLACE FUNCTION log_pricelist_status_change()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    changed_at_utc TIMESTAMPTZ := clock_timestamp();
    previous_status_started_at TIMESTAMPTZ;
    duration_seconds BIGINT;
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        previous_status_started_at := COALESCE(OLD.status_changed_at, OLD.created_at, changed_at_utc);
        duration_seconds := GREATEST(
            0,
            FLOOR(EXTRACT(EPOCH FROM (changed_at_utc - previous_status_started_at)))::BIGINT
        );

        NEW.status_changed_at := changed_at_utc;

        INSERT INTO pricelist_status_history (
            pricelist_id,
            old_status,
            new_status,
            changed_at,
            duration_in_previous_status_seconds
        )
        VALUES (
            OLD.id,
            OLD.status,
            NEW.status,
            changed_at_utc,
            duration_seconds
        );
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_log_pricelist_status_change ON pricelists;

CREATE TRIGGER trg_log_pricelist_status_change
BEFORE UPDATE OF status ON pricelists
FOR EACH ROW
WHEN (OLD.status IS DISTINCT FROM NEW.status)
EXECUTE FUNCTION log_pricelist_status_change();
