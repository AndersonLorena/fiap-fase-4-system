CREATE TABLE brands (
    id         BIGINT       NOT NULL PRIMARY KEY,
    name       VARCHAR(80)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    created_by BIGINT       NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    updated_by BIGINT       NOT NULL,
    CONSTRAINT uk_brands_name UNIQUE (name)
);

CREATE TABLE car_models (
    id         BIGINT       NOT NULL PRIMARY KEY,
    brand_id   BIGINT       NOT NULL,
    name       VARCHAR(80)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    created_by BIGINT       NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    updated_by BIGINT       NOT NULL,
    CONSTRAINT uk_car_models_brand_name UNIQUE (brand_id, name),
    CONSTRAINT fk_car_models_brand FOREIGN KEY (brand_id) REFERENCES brands (id)
);

CREATE INDEX idx_car_models_brand_id ON car_models (brand_id);

CREATE TABLE colors (
    id         BIGINT       NOT NULL PRIMARY KEY,
    name       VARCHAR(80)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    created_by BIGINT       NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    updated_by BIGINT       NOT NULL,
    CONSTRAINT uk_colors_name UNIQUE (name)
);

CREATE TABLE vehicle_years (
    id         BIGINT    NOT NULL PRIMARY KEY,
    year_value INTEGER   NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by BIGINT    NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by BIGINT    NOT NULL,
    CONSTRAINT uk_vehicle_years_value UNIQUE (year_value)
);

CREATE TABLE cars (
    id               BIGINT         NOT NULL PRIMARY KEY,
    brand_id         BIGINT         NOT NULL,
    model_id         BIGINT         NOT NULL,
    color_id         BIGINT         NOT NULL,
    year_id          BIGINT         NOT NULL,
    price            NUMERIC(14, 2) NOT NULL,
    status           VARCHAR(32)    NOT NULL,
    buyer_account_id BIGINT,
    buyer_cpf        VARCHAR(11),
    payment_code     VARCHAR(64),
    sold_at          TIMESTAMP,
    created_at       TIMESTAMP      NOT NULL,
    created_by       BIGINT         NOT NULL,
    updated_at       TIMESTAMP      NOT NULL,
    updated_by       BIGINT         NOT NULL,
    CONSTRAINT fk_cars_brand FOREIGN KEY (brand_id) REFERENCES brands (id),
    CONSTRAINT fk_cars_model FOREIGN KEY (model_id) REFERENCES car_models (id),
    CONSTRAINT fk_cars_color FOREIGN KEY (color_id) REFERENCES colors (id),
    CONSTRAINT fk_cars_year FOREIGN KEY (year_id) REFERENCES vehicle_years (id),
    CONSTRAINT uk_cars_payment_code UNIQUE (payment_code)
);

CREATE INDEX idx_cars_status ON cars (status);
CREATE INDEX idx_cars_price ON cars (price);
CREATE INDEX idx_cars_buyer_account_id ON cars (buyer_account_id);

CREATE TABLE car_photos (
    id         BIGINT       NOT NULL PRIMARY KEY,
    car_id     BIGINT       NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    sort_order INTEGER      NOT NULL,
    CONSTRAINT fk_car_photos_car FOREIGN KEY (car_id) REFERENCES cars (id) ON DELETE CASCADE
);

CREATE INDEX idx_car_photos_car_id ON car_photos (car_id);
