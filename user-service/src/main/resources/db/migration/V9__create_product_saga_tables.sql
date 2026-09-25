CREATE TABLE processed_kafka_events (
                                        id BIGSERIAL PRIMARY KEY,

                                        event_id UUID NOT NULL,
                                        consumer_name VARCHAR(100) NOT NULL,

                                        processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                                        CONSTRAINT uk_processed_kafka_events_event_consumer
                                            UNIQUE (event_id, consumer_name)
);

CREATE INDEX idx_processed_kafka_events_consumer_name
    ON processed_kafka_events(consumer_name);


CREATE TABLE product_outbox_events (
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

CREATE UNIQUE INDEX uk_product_outbox_events_event_id
    ON product_outbox_events(event_id);

CREATE INDEX idx_product_outbox_events_status_created_at
    ON product_outbox_events(status, created_at);

CREATE INDEX idx_product_outbox_events_aggregate_id
    ON product_outbox_events(aggregate_id);