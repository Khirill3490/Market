CREATE TABLE order_outbox_events (
                                     id BIGSERIAL PRIMARY KEY,

                                     event_id UUID NOT NULL,
                                     aggregate_type VARCHAR(100) NOT NULL,
                                     aggregate_id VARCHAR(64) NOT NULL,
                                     event_type VARCHAR(100) NOT NULL,

                                     payload JSONB NOT NULL,

                                     status VARCHAR(30) NOT NULL,
                                     attempts INTEGER NOT NULL DEFAULT 0,
                                     last_error TEXT,

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                     published_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX uk_order_outbox_events_event_id
    ON order_outbox_events(event_id);

CREATE INDEX idx_order_outbox_events_status_created_at
    ON order_outbox_events(status, created_at);

CREATE INDEX idx_order_outbox_events_aggregate_id
    ON order_outbox_events(aggregate_id);