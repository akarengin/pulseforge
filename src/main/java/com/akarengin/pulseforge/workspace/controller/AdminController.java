package com.akarengin.pulseforge.workspace.controller;

import com.akarengin.pulseforge.workspace.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Profile({"local", "test"})
public class AdminController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/workspaces/{workspaceId}/generate-key")
    public ResponseEntity<Map<String, String>> generateApiKey(@PathVariable UUID workspaceId) {
        String apiKey = apiKeyService.generateApiKey(workspaceId);
        return ResponseEntity.ok(Map.of(
            "apiKey", apiKey,
            "warning", "Save this key - it won't be shown again"
        ));
    }
}
