CREATE TABLE regions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    country     VARCHAR(100) NOT NULL,
    latitude    DECIMAL(9,6) NOT NULL,
    longitude   DECIMAL(9,6) NOT NULL,
    area_km2    DECIMAL(10,2),
    created_at  TIMESTAMP DEFAULT NOW()
);
