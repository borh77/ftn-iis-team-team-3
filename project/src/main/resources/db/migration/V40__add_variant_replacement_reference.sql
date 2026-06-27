ALTER TABLE variants
    ADD COLUMN IF NOT EXISTS replacement_variant_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_variants_replacement_variant'
          AND conrelid = 'variants'::regclass
    ) THEN
        ALTER TABLE variants
            ADD CONSTRAINT fk_variants_replacement_variant
            FOREIGN KEY (replacement_variant_id)
            REFERENCES variants(id)
            ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_variants_replacement_variant_id
    ON variants(replacement_variant_id);
