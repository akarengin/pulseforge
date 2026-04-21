-- Add project_id column to events table for fine-grained project-level event scoping
-- All events must belong to both a workspace (tenant) and a project (sub-tenant)

ALTER TABLE events ADD COLUMN project_id UUID NOT NULL;

-- Add foreign key constraint to ensure referential integrity
ALTER TABLE events ADD CONSTRAINT fk_events_project 
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE;

-- Drop old workspace-only index
DROP INDEX IF EXISTS idx_events_workspace_timestamp;

-- Create composite index for efficient queries scoped by workspace AND project
-- Supports queries: WHERE workspace_id = ? AND project_id = ? ORDER BY timestamp DESC
CREATE INDEX idx_events_workspace_project_timestamp ON events(workspace_id, project_id, timestamp DESC);

-- Create index for project-only queries (less common but still needed)
CREATE INDEX idx_events_project ON events(project_id);

-- Comment: Events are now scoped at project level, not just workspace level.
-- This enables fine-grained RBAC where users can have different permissions per project.
