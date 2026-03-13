package com.akarengin.pulseforge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.akarengin.pulseforge.dto.ProjectRequest;
import com.akarengin.pulseforge.dto.ProjectResponse;
import com.akarengin.pulseforge.entity.Project;
import com.akarengin.pulseforge.mapper.ProjectMapper;
import com.akarengin.pulseforge.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectMapper projectMapper;

    @Test
    void createProject_returns201() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        ProjectRequest request = new ProjectRequest("Test Project");
        Project project = Project.builder().id(projectId).name("Test Project").build();
        ProjectResponse response = new ProjectResponse(projectId, workspaceId, "Test Project", Instant.now(), Instant.now());

        when(projectService.createProject(eq(workspaceId), eq("Test Project"))).thenReturn(project);
        when(projectMapper.toResponse(any(Project.class))).thenReturn(response);

        mockMvc.perform(post("/api/workspaces/" + workspaceId + "/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    void getProjects_returnsList() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        ProjectResponse response = new ProjectResponse(UUID.randomUUID(), workspaceId, "P1", Instant.now(), Instant.now());

        when(projectService.getProjectsByWorkspace(workspaceId)).thenReturn(List.of());
        when(projectMapper.toResponseList(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/workspaces/" + workspaceId + "/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("P1"));
    }

    @Test
    void deleteProject_returns204() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();

        doNothing().when(projectService).deleteProject(workspaceId, projectId);

        mockMvc.perform(delete("/api/workspaces/" + workspaceId + "/projects/" + projectId))
                .andExpect(status().isNoContent());
    }
}
