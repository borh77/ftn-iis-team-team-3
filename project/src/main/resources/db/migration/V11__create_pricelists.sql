CREATE TABLE pricelists (
    id BIGSERIAL PRIMARY KEY,
    region_id BIGINT NOT NULL,
    customer_segment VARCHAR(120) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(32) NOT NULL,
    period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_pricelists_region
        FOREIGN KEY (region_id)
        REFERENCES regions(id)
        ON DELETE RESTRICT
);

CREATE TABLE pricelist_items (
    id BIGSERIAL PRIMARY KEY,
    pricelist_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    variant_name VARCHAR(255) NOT NULL,
    CONSTRAINT fk_pricelist_items_pricelist
        FOREIGN KEY (pricelist_id)
        REFERENCES pricelists(id)
        ON DELETE CASCADE
);

CREATE TABLE pricelist_item_thresholds (
    pricelist_item_id BIGINT NOT NULL,
    quantity_from INT NOT NULL,
    quantity_to INT,
    price NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_pricelist_item_thresholds_item
        FOREIGN KEY (pricelist_item_id)
        REFERENCES pricelist_items(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_pricelists_region_id ON pricelists(region_id);
CREATE INDEX idx_pricelist_items_pricelist_id ON pricelist_items(pricelist_id);