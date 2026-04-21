package com.akarengin.pulseforge.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectMembershipResponse(
    UUID id,
    UUID projectId,
    UUID userId,
    String role,
    Instant createdAt
) {
}
