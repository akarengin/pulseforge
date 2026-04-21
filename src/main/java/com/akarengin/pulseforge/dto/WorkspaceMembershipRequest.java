package com.akarengin.pulseforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record WorkspaceMembershipRequest(
        @NotNull(message = "User ID is required")
        UUID userId,

        @NotBlank(message = "Role is required")
        @Pattern(regexp = "OWNER|ADMIN|MEMBER", message = "Role must be OWNER, ADMIN, or MEMBER")
        String role
) {}
