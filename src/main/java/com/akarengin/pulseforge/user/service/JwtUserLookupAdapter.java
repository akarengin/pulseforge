package com.akarengin.pulseforge.user.service;

import com.akarengin.pulseforge.common.security.JwtUserLookup;
import com.akarengin.pulseforge.common.security.UserPrincipal;
import com.akarengin.pulseforge.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUserLookupAdapter implements JwtUserLookup {

    private final UserRepository userRepository;

    @Override
    public Optional<UserPrincipal> findById(UUID userId) {
        return userRepository.findById(userId)
            .map(user -> new UserPrincipal(user.getId(), user.getEmail()));
    }
}