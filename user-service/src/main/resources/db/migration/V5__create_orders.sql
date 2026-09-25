CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        public_id VARCHAR(64) NOT NULL,
                        account_id BIGINT NOT NULL,
                        delivery_address_id BIGINT NOT NULL,
                        status VARCHAR(50) NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL,

                        CONSTRAINT uk_orders_public_id UNIQUE (public_id),

                        CONSTRAINT fk_orders_account
                            FOREIGN KEY (account_id)
                                REFERENCES accounts(id),

                        CONSTRAINT fk_orders_delivery_address
                            FOREIGN KEY (delivery_address_id)
                                REFERENCES addresses(id)
);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL,
                             product_public_id VARCHAR(64) NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             product_image VARCHAR(1024),
                             quantity INTEGER NOT NULL,

                             CONSTRAINT fk_order_items_order
                                 FOREIGN KEY (order_id)
                                     REFERENCES orders(id)
                                     ON DELETE CASCADE,

                             CONSTRAINT chk_order_items_quantity_positive
                                 CHECK (quantity > 0)
);

CREATE INDEX idx_orders_account_id ON orders(account_id);
CREATE INDEX idx_orders_status ON orders(status);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_public_id ON order_items(product_public_id);