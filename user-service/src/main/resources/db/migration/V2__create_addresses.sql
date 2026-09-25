CREATE TABLE addresses (
                           id BIGSERIAL PRIMARY KEY,
                           public_id VARCHAR(64) NOT NULL,
                           account_id BIGINT NOT NULL,
                           label VARCHAR(100),
                           country VARCHAR(100) NOT NULL,
                           region VARCHAR(100),
                           city VARCHAR(100) NOT NULL,
                           street VARCHAR(255) NOT NULL,
                           house VARCHAR(32) NOT NULL,
                           apartment VARCHAR(32),
                           postal_code VARCHAR(32),
                           comment VARCHAR(255),
                           is_default BOOLEAN NOT NULL DEFAULT FALSE,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                           CONSTRAINT uk_addresses_public_id UNIQUE (public_id),
                           CONSTRAINT fk_addresses_account_id
                               FOREIGN KEY (account_id)
                                   REFERENCES accounts(id)
                                   ON DELETE CASCADE
);

CREATE INDEX idx_addresses_account_id ON addresses(account_id);

CREATE UNIQUE INDEX uk_addresses_one_default_per_account
    ON addresses(account_id)
    WHERE is_default = TRUE;