ALTER TABLE pricelists
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS status_changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP;

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
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration_in_previous_status_seconds BIGINT NOT NULL
);

UPDATE pricelist_status_history
SET changed_at = CURRENT_TIMESTAMP
WHERE changed_at IS NULL;

UPDATE pricelist_status_history
SET duration_in_previous_status_seconds = 0
WHERE duration_in_previous_status_seconds IS NULL;

ALTER TABLE pricelist_status_history
    ALTER COLUMN changed_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN changed_at SET NOT NULL,
    ALTER COLUMN duration_in_previous_status_seconds SET NOT NULL;

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

CREATE INDEX IF NOT EXISTS idx_pricelist_status_history_changed_at
    ON pricelist_status_history(changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_pricelist_status_history_old_status_changed_at
    ON pricelist_status_history(old_status, changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_pricelist_status_history_new_status_changed_at
    ON pricelist_status_history(new_status, changed_at DESC);

CREATE INDEX IF NOT EXISTS idx_pricelist_status_history_duration_desc
    ON pricelist_status_history(duration_in_previous_status_seconds DESC);

CREATE OR REPLACE FUNCTION log_pricelist_status_change()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    changed_at TIMESTAMPTZ := clock_timestamp();
    previous_status_started_at TIMESTAMPTZ;
    duration_seconds BIGINT;
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        previous_status_started_at := COALESCE(OLD.status_changed_at, OLD.created_at, changed_at);
        duration_seconds := GREATEST(
            0,
            FLOOR(EXTRACT(EPOCH FROM (changed_at - previous_status_started_at)))::BIGINT
        );

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
            changed_at,
            duration_seconds
        );

        NEW.status_changed_at := changed_at;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_log_pricelist_status_change ON pricelists;
DROP TRIGGER IF EXISTS trg_pricelist_status_change ON pricelists;

CREATE TRIGGER trg_pricelist_status_change
BEFORE UPDATE OF status ON pricelists
FOR EACH ROW
WHEN (OLD.status IS DISTINCT FROM NEW.status)
EXECUTE FUNCTION log_pricelist_status_change();

-- Manual verification:
-- SELECT * FROM pricelist_status_history ORDER BY changed_at DESC LIMIT 20;
-- UPDATE pricelists SET status = 'IN_REVIEW' WHERE id = 1 AND status IS DISTINCT FROM 'IN_REVIEW';
-- SELECT * FROM pricelist_status_history WHERE pricelist_id = 1 ORDER BY changed_at DESC;
