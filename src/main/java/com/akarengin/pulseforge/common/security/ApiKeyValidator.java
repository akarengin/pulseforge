package com.akarengin.pulseforge.common.security;

public interface ApiKeyValidator {
    WorkspaceIdentity validateApiKey(String plainKey);
}
