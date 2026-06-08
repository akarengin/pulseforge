package com.akarengin.pulseforge.dto;

import java.util.UUID;

import com.akarengin.pulseforge.entity.ProjectRole;

import jakarta.validation.constraints.NotNull;

public record ProjectMembershipRequest(
    @NotNull(message = "Role is required")
    ProjectRole role,

    @NotNull(message = "User ID is required")
    UUID userId
) {
}
