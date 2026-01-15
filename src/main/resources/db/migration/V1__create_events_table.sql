-- Single events table to validate DB setup and Spring config

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    payload JSONB,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_events_timestamp ON events(timestamp);
