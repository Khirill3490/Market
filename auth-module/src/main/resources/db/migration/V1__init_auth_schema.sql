CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       public_id VARCHAR(255) UNIQUE,
                       email VARCHAR(255) UNIQUE,
                       password VARCHAR(255),
                       is_active BOOLEAN NOT NULL,
                       role VARCHAR(255),
                       rules VARCHAR(255)
);

CREATE TABLE companies (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255),
                           inn VARCHAR(255) NOT NULL UNIQUE,
                           city VARCHAR(255),
                           phone VARCHAR(255),
                           mail VARCHAR(255),
                           url VARCHAR(255),
                           rules VARCHAR(255),
                           logo VARCHAR(255),
                           tax DOUBLE PRECISION,
                           rating DOUBLE PRECISION,
                           user_id BIGINT UNIQUE,
                           CONSTRAINT fk_companies_user
                               FOREIGN KEY (user_id) REFERENCES users(id)
);