package com.akarengin.pulseforge.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventResponse(
    UUID id,
    UUID workspaceId,
    String type,
    Map<String, Object> payload,
    Instant timestamp
) {

}
