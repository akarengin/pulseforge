package com.akarengin.pulseforge.security;

import com.akarengin.pulseforge.entity.Workspace;
import java.util.UUID;

/**
 * Principal stored in SecurityContextHolder after API key authentication.
 * Contains the resolved Workspace for the authenticated request.
 */
public record ApiKeyPrincipal(Workspace workspace) {

    public UUID getWorkspaceId() { return workspace.getId(); }
}
