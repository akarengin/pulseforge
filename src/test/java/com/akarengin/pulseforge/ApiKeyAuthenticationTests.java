package com.akarengin.pulseforge;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.project.repository.ProjectRepository;
import com.akarengin.pulseforge.workspace.repository.WorkspaceRepository;
import com.akarengin.pulseforge.workspace.service.ApiKeyService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiKeyAuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void shouldCreateEventWithValidApiKey() throws Exception {
        Workspace workspace = workspaceRepository.save(
            Workspace.builder().name("Test Workspace 1").createdAt(Instant.now()).build()
        );
        Project project = projectRepository.save(
            Project.builder().workspace(workspace).name("Test Project 1").createdAt(Instant.now())
                .build()
        );
        String apiKey = apiKeyService.generateApiKey(workspace.getId());

        String eventJson = """
            {
                "type": "test_event",
                "payload": {"action": "click"},
                "idempotencyKey": "dedup-key-123"
            }
            """;

        mockMvc.perform(post(
                "/api/workspaces/" + workspace.getId() + "/projects/" + project.getId() + "/events")
                .header("X-API-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventJson))
            .andExpect(status().isAccepted());
    }

    @Test
    void shouldReject401WithInvalidApiKey() throws Exception {
        Workspace workspace = workspaceRepository.save(
            Workspace.builder().name("Test Workspace 2").createdAt(Instant.now()).build()
        );
        Project project = projectRepository.save(
            Project.builder().workspace(workspace).name("Test Project 2").createdAt(Instant.now())
                .build()
        );

        String eventJson = """
            {
                "type": "test_event",
                "payload": {"action": "click"},
                "idempotencyKey": "dedup-key-123"
            }
            """;

        mockMvc.perform(post(
                "/api/workspaces/" + workspace.getId() + "/projects/" + project.getId() + "/events")
                .header("X-API-Key", "invalid-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventJson))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReject401WithMissingApiKey() throws Exception {
        mockMvc.perform(post(
                "/api/workspaces/1/projects/1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReject403WithMismatchedWorkspace() throws Exception {
        Workspace workspaceA = workspaceRepository.save(
            Workspace.builder().name("Workspace A").createdAt(Instant.now()).build()
        );
        Workspace workspaceB = workspaceRepository.save(
            Workspace.builder().name("Workspace B").createdAt(Instant.now()).build()
        );

        Project projectA = projectRepository.save(
            Project.builder().workspace(workspaceA).name("Project A").createdAt(Instant.now()).build()
        );

        String apiKeyA = apiKeyService.generateApiKey(workspaceA.getId());
        String apiKeyB = apiKeyService.generateApiKey(workspaceB.getId());

        String eventJson = """
            {
                "type": "test_event",
                "payload": {"action": "click"},
                "idempotencyKey": "dedup-key-123"
            }
            """;

        mockMvc.perform(post(
                "/api/workspaces/" + workspaceA.getId() + "/projects/" + projectA.getId() + "/events")
                .header("X-API-Key", apiKeyA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventJson))
            .andExpect(status().isAccepted());

        mockMvc.perform(post(
                "/api/workspaces/" + workspaceA.getId() + "/projects/" + projectA.getId() + "/events")
                .header("X-API-Key", apiKeyB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(eventJson))
            .andExpect(status().isForbidden());
    }
}
