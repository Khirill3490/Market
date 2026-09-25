CREATE TABLE order_status_history (
                                      id BIGSERIAL PRIMARY KEY,

                                      order_id BIGINT NOT NULL,

                                      from_status VARCHAR(50),
                                      to_status VARCHAR(50) NOT NULL,

                                      reason TEXT,

                                      changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                      CONSTRAINT fk_order_status_history_order
                                          FOREIGN KEY (order_id)
                                              REFERENCES orders(id)
                                              ON DELETE CASCADE
);

CREATE INDEX idx_order_status_history_order_changed_at
    ON order_status_history(order_id, changed_at);