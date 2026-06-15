package com.akarengin.pulseforge.common.security;

import java.util.UUID;

/**
 * Principal stored in SecurityContextHolder after API key authentication.
 * Contains the resolved workspace identity for the authenticated request.
 */
public record ApiKeyPrincipal(WorkspaceIdentity workspaceIdentity) {

    public UUID getWorkspaceId() { return workspaceIdentity.workspaceId(); }
}
