package com.akarengin.pulseforge.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkspaceMembershipResponse(
        UUID id,
        UUID workspaceId,
        UUID userId,
        String userEmail,
        String role,
        Instant createdAt
) {}
