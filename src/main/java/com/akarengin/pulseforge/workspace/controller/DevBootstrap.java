package com.akarengin.pulseforge.workspace.controller;

import com.akarengin.pulseforge.workspace.service.ApiKeyService;
import com.akarengin.pulseforge.workspace.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DevBootstrap implements ApplicationRunner {
    
    private final WorkspaceService workspaceService;
    private final ApiKeyService apiKeyService;
    
    @Override
    public void run(ApplicationArguments args) {
        // This method is executed automatically when the Spring Boot application starts up
        // It creates a development workspace and generates an API key for local development
        var workspace = workspaceService.createWorkspace("Dev Workspace");

        String apiKey = apiKeyService.generateApiKey(workspace.getId());

        log.info("=".repeat(60));
        log.info("DEV WORKSPACE CREATED");
        log.info("Workspace ID: {}", workspace.getId());
        log.info("API Key: {}", apiKey);
        log.info("=".repeat(60));
    }
}
