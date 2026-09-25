CREATE TABLE brands (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        ps_name VARCHAR(255),
                        url VARCHAR(1024),

                        CONSTRAINT uk_brands_name UNIQUE (name)
);

CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,

                          CONSTRAINT uk_category_name UNIQUE (name)
);

CREATE TABLE products (
                          id BIGSERIAL PRIMARY KEY,
                          art VARCHAR(255),
                          man BIGINT NOT NULL,
                          name VARCHAR(255),
                          inf TEXT,
                          ext TEXT,
                          img VARCHAR(1024),
                          url VARCHAR(1024),
                          unit VARCHAR(64),
                          sml VARCHAR(255),
                          category_id BIGINT NOT NULL,
                          bar VARCHAR(255),

                          CONSTRAINT fk_products_brand
                              FOREIGN KEY (man)
                                  REFERENCES brands(id),

                          CONSTRAINT fk_products_category
                              FOREIGN KEY (category_id)
                                  REFERENCES category(id)
);

CREATE INDEX idx_product_art ON products(art);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_man ON products(man);