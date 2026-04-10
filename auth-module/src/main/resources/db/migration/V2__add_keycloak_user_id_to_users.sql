ALTER TABLE users
    ADD COLUMN keycloak_user_id VARCHAR(255);

CREATE UNIQUE INDEX uk_users_keycloak_user_id
    ON users(keycloak_user_id);