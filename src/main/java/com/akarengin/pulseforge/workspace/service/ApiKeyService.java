package com.akarengin.pulseforge.workspace.service;

import com.akarengin.pulseforge.common.security.ApiKeyValidator;
import com.akarengin.pulseforge.common.security.WorkspaceIdentity;
import com.akarengin.pulseforge.workspace.entity.ApiKey;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.workspace.repository.ApiKeyRepository;
import com.akarengin.pulseforge.workspace.repository.WorkspaceRepository;
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
public class ApiKeyService implements ApiKeyValidator {
    private final ApiKeyRepository apiKeyRepository;
    private final WorkspaceRepository workspaceRepository;

    public String generateApiKey(UUID workspaceId) {
        String key = UUID.randomUUID().toString();
        String hashKey = hashKey(key);
        Workspace workspace = workspaceRepository.findById(workspaceId).orElse(null);
        apiKeyRepository.save(ApiKey.builder().workspace(workspace).keyHash(hashKey).build());
        return key;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkspaceIdentity validateApiKey(String plainKey) {
        String hashKey = hashKey(plainKey);
        Optional<ApiKey> apiKey = apiKeyRepository.findByKeyHash(hashKey);
        if (apiKey.isPresent()) {
            Workspace workspace = apiKey.get().getWorkspace();
            workspace.getName();
            return new WorkspaceIdentity(workspace.getId());
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
