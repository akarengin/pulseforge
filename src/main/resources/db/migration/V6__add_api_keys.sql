-- V6__add_api_keys.sql
-- Authentication & tenant extraction
-- Purpose: Store SHA-256 hashed API keys for workspace authentication

CREATE TABLE api_keys (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    key_hash VARCHAR(64) NOT NULL UNIQUE,  -- SHA-256 produces 64 hex characters
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_api_keys_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
);

CREATE INDEX idx_api_keys_workspace_id ON api_keys(workspace_id);

-- Design notes:
-- - SHA-256 hashing (not BCrypt) for performance on per-request lookups
-- - key_hash is unique to prevent duplicate keys
-- - Cascade delete when workspace is deleted
-- - Index on workspace_id for efficient lookups
