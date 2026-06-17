ALTER TABLE pricelists
    ADD COLUMN version_number INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN parent_pricelist_id BIGINT,
    ADD COLUMN root_pricelist_id BIGINT;

CREATE INDEX idx_pricelists_parent_pricelist_id ON pricelists(parent_pricelist_id);
CREATE INDEX idx_pricelists_root_pricelist_id ON pricelists(root_pricelist_id);
