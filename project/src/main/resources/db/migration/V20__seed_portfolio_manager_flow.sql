-- Fresh seed for a minimal Portfolio Manager flow.
-- Creates users 1..5 so the Portfolio Manager user exists with id = 5,
-- then inserts one minimal chain of entities for pricelist creation.

INSERT INTO users (username, password_hash, email, role, is_active, has_changed_password, first_name, last_name)
VALUES
  ('bootstrap_admin', '$2a$10$dummybootstrapadminhash', 'bootstrap_admin@example.com', 'ROLE_ADMIN', true, true, 'Bootstrap', 'Admin'),
  ('bootstrap_user1', '$2a$10$dummybootstrapuser1hash', 'bootstrap_user1@example.com', 'ROLE_BUYER', true, true, 'Bootstrap', 'User1'),
  ('bootstrap_user2', '$2a$10$dummybootstrapuser2hash', 'bootstrap_user2@example.com', 'ROLE_PORTFOLIO_MANAGER', true, true, 'Bootstrap', 'User2'),
  ('bootstrap_user3', '$2a$10$dummybootstrapuser3hash', 'bootstrap_user3@example.com', 'ROLE_PORTFOLIO_MANAGER', true, true, 'Bootstrap', 'User3'),
  ('portfolio_manager', '$2a$10$dummyportfoliohash', 'portfolio_manager@example.com', 'ROLE_PORTFOLIO_MANAGER', true, true, 'Portfolio', 'Manager')
ON CONFLICT (username) DO NOTHING;

INSERT INTO categories (name, description, status, created_by, created_at)
VALUES ('PM_Category', 'Category for portfolio manager pricing flow', 'ACTIVE', 5, NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO subcategories (category_id, name, description, status, created_by, created_at)
SELECT c.id, 'PM_Subcategory', 'Subcategory for portfolio manager', 'ACTIVE', 5, NOW()
FROM categories c
WHERE c.name = 'PM_Category'
  AND NOT EXISTS (
    SELECT 1 FROM subcategories sc WHERE sc.category_id = c.id AND sc.name = 'PM_Subcategory'
  );

INSERT INTO therapeutic_areas (name, description, status, created_by, created_at)
VALUES ('PM_Therapeutic_Area', 'Therapeutic area for PM flow', 'ACTIVE', 5, NOW())
ON CONFLICT (name) DO NOTHING;

INSERT INTO ingredients (name, chemical_formula, type, cas, status, created_by, created_at)
VALUES ('PM_Ingredient', NULL, 'API', 'PM-CAS-0001', 'ACTIVE', 5, NOW())
ON CONFLICT (cas) DO NOTHING;

INSERT INTO products (name, description, subcategory_id, therapeutic_area_id, status, created_by, created_at)
SELECT 'PM_Product', 'Product for portfolio manager flow', sc.id, ta.id, 'ACTIVE', 5, NOW()
FROM subcategories sc, therapeutic_areas ta
WHERE sc.name = 'PM_Subcategory'
  AND ta.name = 'PM_Therapeutic_Area'
  AND NOT EXISTS (
    SELECT 1 FROM products p WHERE p.name = 'PM_Product'
  );

INSERT INTO variants (product_id, form, dosage, status, created_by, created_at)
SELECT p.id, 'tablet', '100mg', 'ACTIVE', 5, NOW()
FROM products p
WHERE p.name = 'PM_Product'
  AND NOT EXISTS (
    SELECT 1 FROM variants v WHERE v.product_id = p.id AND v.form = 'tablet' AND v.dosage = '100mg'
  );

INSERT INTO variant_versions (variant_id, version_label, description, status, created_by, created_at)
SELECT v.id, 'v1', 'Initial PM version', 'ACTIVE', 5, NOW()
FROM variants v
JOIN products p ON v.product_id = p.id
WHERE p.name = 'PM_Product'
  AND NOT EXISTS (
    SELECT 1 FROM variant_versions vv WHERE vv.variant_id = v.id AND vv.version_label = 'v1'
  );

INSERT INTO variant_version_ingredients (variant_version_id, ingredient_id, amount, unit, created_by, created_at)
SELECT vv.id, i.id, 100, 'mg', 5, NOW()
FROM variant_versions vv
JOIN variants v ON vv.variant_id = v.id
JOIN products p ON v.product_id = p.id
JOIN ingredients i ON i.name = 'PM_Ingredient'
WHERE p.name = 'PM_Product'
  AND NOT EXISTS (
    SELECT 1 FROM variant_version_ingredients x WHERE x.variant_version_id = vv.id AND x.ingredient_id = i.id
  );

INSERT INTO pricelists (region_id, customer_segment, currency, status, period_start, period_end)
SELECT r.id, 'Retail', 'RSD', 'ACTIVE', NOW(), NOW() + INTERVAL '30 days'
FROM regions r
WHERE r.name = 'Beograd'
  AND NOT EXISTS (
    SELECT 1 FROM pricelists pl WHERE pl.region_id = r.id AND pl.customer_segment = 'Retail' AND pl.currency = 'RSD'
  );

INSERT INTO pricelist_items (pricelist_id, variant_id, variant_name)
SELECT pl.id, v.id, 'PM_Product tablet 100mg'
FROM pricelists pl
JOIN variants v ON v.product_id = (SELECT id FROM products WHERE name = 'PM_Product' LIMIT 1)
WHERE pl.customer_segment = 'Retail'
  AND pl.currency = 'RSD'
  AND NOT EXISTS (
    SELECT 1 FROM pricelist_items pi WHERE pi.pricelist_id = pl.id AND pi.variant_id = v.id
  );

INSERT INTO pricelist_item_thresholds (pricelist_item_id, quantity_from, quantity_to, price)
SELECT pi.id, 1, 10, 120.00
FROM pricelist_items pi
JOIN pricelists pl ON pi.pricelist_id = pl.id
JOIN variants v ON pi.variant_id = v.id
JOIN products p ON v.product_id = p.id
WHERE p.name = 'PM_Product'
  AND NOT EXISTS (
    SELECT 1 FROM pricelist_item_thresholds t WHERE t.pricelist_item_id = pi.id AND t.quantity_from = 1
  );

SELECT
  (SELECT id FROM users WHERE username = 'portfolio_manager') AS portfolio_manager_id,
  (SELECT id FROM categories WHERE name = 'PM_Category') AS category_id,
  (SELECT id FROM subcategories WHERE name = 'PM_Subcategory' AND category_id = (SELECT id FROM categories WHERE name = 'PM_Category')) AS subcategory_id,
  (SELECT id FROM therapeutic_areas WHERE name = 'PM_Therapeutic_Area') AS therapeutic_area_id,
  (SELECT id FROM ingredients WHERE name = 'PM_Ingredient') AS ingredient_id,
  (SELECT id FROM products WHERE name = 'PM_Product') AS product_id,
  (SELECT id FROM variants WHERE product_id = (SELECT id FROM products WHERE name = 'PM_Product') AND form = 'tablet' AND dosage = '100mg') AS variant_id,
  (SELECT id FROM variant_versions WHERE variant_id = (SELECT id FROM variants WHERE product_id = (SELECT id FROM products WHERE name = 'PM_Product') LIMIT 1) AND version_label = 'v1') AS variant_version_id;
