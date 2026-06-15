package com.akarengin.pulseforge.workspace.dto;

import java.util.UUID;

import com.akarengin.pulseforge.workspace.entity.WorkspaceRole;

import jakarta.validation.constraints.NotNull;

public record WorkspaceMembershipRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Role is required")
        WorkspaceRole role
) {
}
