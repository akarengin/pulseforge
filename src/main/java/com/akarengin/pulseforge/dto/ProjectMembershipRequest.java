package com.akarengin.pulseforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record ProjectMembershipRequest(
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(MANAGER|CONTRIBUTOR|VIEWER)$", message = "Role must be one of: OWNER, ADMIN, MEMBER, VIEWER")
    String role,

    @NotNull(message = "User ID is required")
    UUID userId
) {
}
