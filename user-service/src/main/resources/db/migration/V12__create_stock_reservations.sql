CREATE TABLE stock_reservations (
                                    id BIGSERIAL PRIMARY KEY,

                                    order_public_id VARCHAR(64) NOT NULL,

                                    product_id BIGINT NOT NULL,
                                    product_public_id VARCHAR(64) NOT NULL,

                                    quantity INTEGER NOT NULL,
                                    status VARCHAR(30) NOT NULL,

                                    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                    CONSTRAINT fk_stock_reservations_product
                                        FOREIGN KEY (product_id)
                                            REFERENCES products(id),

                                    CONSTRAINT uk_stock_reservations_order_product
                                        UNIQUE (order_public_id, product_public_id),

                                    CONSTRAINT chk_stock_reservations_quantity_positive
                                        CHECK (quantity > 0)
);

CREATE INDEX idx_stock_reservations_order_public_id
    ON stock_reservations(order_public_id);

CREATE INDEX idx_stock_reservations_status_created_at
    ON stock_reservations(status, created_at);