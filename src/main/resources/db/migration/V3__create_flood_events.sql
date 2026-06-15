CREATE TABLE flood_events (
    id              BIGSERIAL PRIMARY KEY,
    region_id       BIGINT REFERENCES regions(id) ON DELETE CASCADE,
    event_type      VARCHAR(20) NOT NULL,
    severity        VARCHAR(20) NOT NULL,
    risk_score      DECIMAL(4,2),
    started_at      TIMESTAMP,
    ended_at        TIMESTAMP,
    description     TEXT,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_flood_events_region_id ON flood_events(region_id);
CREATE INDEX idx_flood_events_severity ON flood_events(severity);
