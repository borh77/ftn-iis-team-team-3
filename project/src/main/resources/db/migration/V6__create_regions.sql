CREATE TABLE regions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL
);

INSERT INTO regions (name, code) VALUES
    ('Vojvodina', 'RS'),
    ('Beograd', 'RS');