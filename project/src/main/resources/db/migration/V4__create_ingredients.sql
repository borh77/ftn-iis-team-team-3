CREATE TABLE ingredients
(
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    chemical_formula VARCHAR(100),
    type VARCHAR(30) NOT NULL,
    cas VARCHAR(50) NOT NULL UNIQUE,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',    
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMP
);