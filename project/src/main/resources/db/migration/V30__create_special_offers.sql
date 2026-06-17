CREATE TABLE special_offers (
    id BIGSERIAL PRIMARY KEY,
    pricelist_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    variant_name VARCHAR(255) NOT NULL,
    discount_type VARCHAR(32) NOT NULL,
    discount_value NUMERIC(19, 2) NOT NULL,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_special_offers_pricelist
        FOREIGN KEY (pricelist_id)
        REFERENCES pricelists(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_special_offers_discount_value_positive
        CHECK (discount_value > 0),
    CONSTRAINT chk_special_offers_period
        CHECK (start_date < end_date)
);

CREATE INDEX idx_special_offers_pricelist_id ON special_offers(pricelist_id);
CREATE INDEX idx_special_offers_active_lookup ON special_offers(pricelist_id, variant_id, status, start_date, end_date);
