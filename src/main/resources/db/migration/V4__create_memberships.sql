-- RBAC - User-Workspace-Role relationship

CREATE TABLE memberships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (workspace_id, user_id)
);

CREATE INDEX idx_membership_workspace ON memberships(workspace_id);
CREATE INDEX idx_membership_user ON memberships(user_id);
