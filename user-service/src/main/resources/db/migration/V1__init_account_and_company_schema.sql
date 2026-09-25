CREATE TABLE accounts (
                          id BIGSERIAL PRIMARY KEY,
                          public_id VARCHAR(64) NOT NULL,
                          keycloak_user_id VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL,
                          first_name VARCHAR(100),
                          last_name VARCHAR(100),
                          phone VARCHAR(32),
                          account_type VARCHAR(32) NOT NULL,
                          status VARCHAR(32) NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL,
                          updated_at TIMESTAMPTZ NOT NULL,

                          CONSTRAINT uk_accounts_public_id UNIQUE (public_id),
                          CONSTRAINT uk_accounts_keycloak_user_id UNIQUE (keycloak_user_id),
                          CONSTRAINT uk_accounts_email UNIQUE (email)
);

CREATE TABLE companies (
                           id BIGSERIAL PRIMARY KEY,
                           public_id VARCHAR(64) NOT NULL,
                           owner_account_id BIGINT NOT NULL,
                           name VARCHAR(255) NOT NULL,
                           inn VARCHAR(32) NOT NULL,
                           status VARCHAR(32) NOT NULL,
                           contact_email VARCHAR(255),
                           contact_phone VARCHAR(32),
                           created_at TIMESTAMPTZ NOT NULL,
                           updated_at TIMESTAMPTZ NOT NULL,

                           CONSTRAINT uk_companies_public_id UNIQUE (public_id),
                           CONSTRAINT uk_companies_inn UNIQUE (inn),
                           CONSTRAINT uk_companies_owner_account_id UNIQUE (owner_account_id),

                           CONSTRAINT fk_companies_owner_account_id
                               FOREIGN KEY (owner_account_id) REFERENCES accounts(id) ON DELETE CASCADE
);

CREATE INDEX idx_companies_owner_account_id ON companies(owner_account_id);