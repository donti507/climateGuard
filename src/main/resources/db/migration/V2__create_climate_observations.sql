CREATE TABLE climate_observations (
    id              BIGSERIAL PRIMARY KEY,
    region_id       BIGINT REFERENCES regions(id) ON DELETE CASCADE,
    observed_at     TIMESTAMP NOT NULL,
    temperature_c   DECIMAL(5,2),
    rainfall_mm     DECIMAL(7,2),
    humidity_pct    DECIMAL(5,2),
    wind_speed_ms   DECIMAL(6,2),
    source          VARCHAR(50),
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_observations_region_id ON climate_observations(region_id);
CREATE INDEX idx_observations_observed_at ON climate_observations(observed_at);
