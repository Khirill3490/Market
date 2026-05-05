ALTER TABLE products
    ADD COLUMN public_id VARCHAR(64);

UPDATE products
SET public_id = REPLACE(gen_random_uuid()::text, '-', '')
WHERE public_id IS NULL;

ALTER TABLE products
    ALTER COLUMN public_id SET NOT NULL;

ALTER TABLE products
    ADD CONSTRAINT uk_products_public_id UNIQUE (public_id);

CREATE INDEX idx_products_public_id ON products(public_id);

-- если не сработает применяем ниже

-- CREATE EXTENSION IF NOT EXISTS pgcrypto;
--
-- ALTER TABLE products
--     ADD COLUMN public_id VARCHAR(64);
--
-- UPDATE products
-- SET public_id = REPLACE(gen_random_uuid()::text, '-', '')
-- WHERE public_id IS NULL;
--
-- ALTER TABLE products
--     ALTER COLUMN public_id SET NOT NULL;
--
-- ALTER TABLE products
--     ADD CONSTRAINT uk_products_public_id UNIQUE (public_id);
--
-- CREATE INDEX idx_products_public_id ON products(public_id);