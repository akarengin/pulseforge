-- Link events to workspaces for multi-tenancy
-- Add workspace_id to events table and create composite index

ALTER TABLE events
ADD COLUMN workspace_id UUID NOT NULL;

ALTER TABLE events
ADD CONSTRAINT fk_events_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE;

-- Composite index for tenant-scoped time-range queries
-- Supports: WHERE workspace_id = ? AND timestamp BETWEEN ? AND ?
CREATE INDEX idx_events_workspace_timestamp ON events(workspace_id, timestamp);
