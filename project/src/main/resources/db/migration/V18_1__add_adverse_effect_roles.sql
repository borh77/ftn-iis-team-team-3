-- Test users for adverse effect reports
-- Password for all users: Password1!
INSERT INTO users (username, password_hash, email, first_name, last_name, role, is_active, has_changed_password)
VALUES
    ('lekar1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17ldy.', 'lekar1@iis.com', 'Marko', 'Markovic', 'ROLE_LEKAR', true, true),
    ('pacijent1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17ldy.', 'pacijent1@iis.com', 'Ana', 'Anic', 'ROLE_PACIJENT', true, true),
    ('farmakovigilant1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17ldy.', 'farmakovigilant1@iis.com', 'Jovana', 'Jovanovic', 'ROLE_FARMAKOVIGILANT', true, true)
ON CONFLICT (username) DO NOTHING;

