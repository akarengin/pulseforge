-- Single events table to validate DB setup and Spring config

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_events_timestamp ON events(timestamp);
