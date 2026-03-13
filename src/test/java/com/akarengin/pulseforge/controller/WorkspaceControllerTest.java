package com.akarengin.pulseforge.controller;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.akarengin.pulseforge.dto.WorkspaceRequestDTO;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.service.WorkspaceService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private WorkspaceService workspaceService;

    @Test
    void createWorkspace_returns201AndBody_andSetsLocation() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var created = Workspace.builder()
            .id(workspaceId)
            .name("acme")
            .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .build();

        when(workspaceService.createWorkspace("acme")).thenReturn(created);

        var request = new WorkspaceRequestDTO("acme");

        mockMvc.perform(
                post("/api/workspaces")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/api/workspaces/" + workspaceId))
            .andExpect(jsonPath("$.id").value(workspaceId.toString()))
            .andExpect(jsonPath("$.name").value("acme"));
    }

    @Test
    void getWorkspace_found_returns200AndBody() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var workspace = Workspace.builder()
            .id(workspaceId)
            .name("acme")
            .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .build();

        when(workspaceService.getWorkspaceById(workspaceId)).thenReturn(Optional.of(workspace));

        mockMvc.perform(get("/api/workspaces/" + workspaceId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(workspaceId.toString()))
            .andExpect(jsonPath("$.name").value("acme"));
    }

    @Test
    void getWorkspace_notFound_returns404() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000404");
        when(workspaceService.getWorkspaceById(workspaceId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/workspaces/" + workspaceId))
            .andExpect(status().isNotFound());
    }
}
