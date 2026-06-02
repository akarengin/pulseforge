package com.akarengin.pulseforge.service;

import com.akarengin.pulseforge.entity.ApiKey;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.repository.ApiKeyRepository;
import com.akarengin.pulseforge.repository.WorkspaceRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final WorkspaceRepository workspaceRepository;

    public String generateApiKey(UUID workspaceId) {
        String key = UUID.randomUUID().toString();
        String hashKey = hashKey(key);
        Workspace workspace = workspaceRepository.findById(workspaceId).orElse(null);
        apiKeyRepository.save(ApiKey.builder().workspace(workspace).keyHash(hashKey).build());
        return key;
    }

    @Transactional(readOnly = true)
    public Workspace validateApiKey(String plainKey) {
        String hashKey = hashKey(plainKey);
        Optional<ApiKey> apiKey = apiKeyRepository.findByKeyHash(hashKey);
        if (apiKey.isPresent()) {
            Workspace workspace = apiKey.get().getWorkspace();
            // ↑ workspace field is a proxy, no DB query yet (JPA LAZY)
            // Force-initialize the lazy proxy within the transaction
            workspace.getName();
            return workspace;
        }
        return null;
    }

    private String hashKey(String plainKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
