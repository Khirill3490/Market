CREATE TABLE carts (
                       id BIGSERIAL PRIMARY KEY,
                       public_id VARCHAR(64) NOT NULL,
                       account_id BIGINT NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                       CONSTRAINT uk_carts_public_id UNIQUE (public_id),
                       CONSTRAINT uk_carts_account_id UNIQUE (account_id),
                       CONSTRAINT fk_carts_account_id
                           FOREIGN KEY (account_id)
                               REFERENCES accounts(id)
                               ON DELETE CASCADE
);

CREATE INDEX idx_carts_account_id ON carts(account_id);

CREATE TABLE cart_items (
                            id BIGSERIAL PRIMARY KEY,
                            public_id VARCHAR(64) NOT NULL,
                            cart_id BIGINT NOT NULL,
                            product_public_id VARCHAR(64) NOT NULL,
                            quantity INTEGER NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                            updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                            CONSTRAINT uk_cart_items_public_id UNIQUE (public_id),
                            CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_public_id),
                            CONSTRAINT fk_cart_items_cart_id
                                FOREIGN KEY (cart_id)
                                    REFERENCES carts(id)
                                    ON DELETE CASCADE,
                            CONSTRAINT chk_cart_items_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_public_id ON cart_items(product_public_id);