ALTER TABLE events ADD COLUMN idempotency_key VARCHAR(255) NOT NULL;

--ALTER TABLE events ALTER COLUMN idempotency_key DROP DEFAULT;
ALTER TABLE events ADD CONSTRAINT uk_workspace_idempotency UNIQUE (workspace_id, idempotency_key);