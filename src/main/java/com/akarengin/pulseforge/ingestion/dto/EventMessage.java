package com.akarengin.pulseforge.ingestion.dto;

import java.util.Map;
import java.util.UUID;

public record EventMessage(
    String type,
    Map<String, Object> payload,
    UUID workspaceId,
    UUID projectId
) {
}
