package com.akarengin.pulseforge.common.security;

import java.util.UUID;

public record UserPrincipal(UUID userId, String email) {}
