-- Test korisnici za podsistem neželjenih efekata lekova
-- Lozinka za sve: Password1! (bcrypt hash)
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active, has_changed_password)
VALUES
    ('lekar1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17ldy.', 'lekar1@iis.com', 'Marko', 'Marković', 'ROLE_LEKAR', true, true),
    ('pacijent1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17ldy.', 'pacijent1@iis.com', 'Ana', 'Anić', 'ROLE_PACIJENT', true, true),
    ('farmakovigilant1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17ldy.', 'farmakovigilant1@iis.com', 'Jovana', 'Jovanović', 'ROLE_FARMAKOVIGILANT', true, true);
