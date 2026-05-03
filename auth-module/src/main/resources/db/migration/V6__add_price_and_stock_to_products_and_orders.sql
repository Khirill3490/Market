ALTER TABLE products
    ADD COLUMN price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN stock_quantity INTEGER NOT NULL DEFAULT 0;

ALTER TABLE products
    ADD CONSTRAINT chk_products_stock_quantity_non_negative
        CHECK (stock_quantity >= 0);

ALTER TABLE order_items
    ADD COLUMN unit_price NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN total_price NUMERIC(12, 2) NOT NULL DEFAULT 0.00;