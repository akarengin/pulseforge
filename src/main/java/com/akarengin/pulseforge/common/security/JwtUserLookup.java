package com.akarengin.pulseforge.common.security;

import java.util.Optional;
import java.util.UUID;

public interface JwtUserLookup {

    Optional<UserPrincipal> findById(UUID userId);
}