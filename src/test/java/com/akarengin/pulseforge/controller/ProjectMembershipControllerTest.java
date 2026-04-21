package com.akarengin.pulseforge.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.akarengin.pulseforge.dto.ProjectMembershipResponse;
import com.akarengin.pulseforge.entity.Project;
import com.akarengin.pulseforge.entity.ProjectMembership;
import com.akarengin.pulseforge.entity.ProjectRole;
import com.akarengin.pulseforge.entity.User;
import com.akarengin.pulseforge.mapper.ProjectMembershipMapper;
import com.akarengin.pulseforge.service.ProjectMembershipService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectMembershipController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectMembershipService projectMembershipService;

    @MockitoBean
    private ProjectMembershipMapper projectMembershipMapper;

@Test
void testAddMember() throws Exception {
    UUID workspaceId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = User.builder()
        .id(userId)
        .email("user@example.com")
        .build();

    Project project = Project.builder()
        .id(projectId)
        .name("Test Project")
        .build();

    ProjectMembership membership = ProjectMembership.builder()
        .id(UUID.randomUUID())
        .project(project)
        .user(user)
        .role(ProjectRole.VIEWER)
        .build();

    ProjectMembershipResponse response = new ProjectMembershipResponse(
        membership.getId(), projectId, userId, "VIEWER", null
    );

    when(projectMembershipService.addUserToProject(workspaceId, projectId, userId, "VIEWER")).thenReturn(membership);
    when(projectMembershipMapper.toResponse(membership)).thenReturn(response);

    var requestJson = """
        {
            "userId": "%s",
            "role":"VIEWER"
        }
        """.formatted(userId);

    mockMvc.perform(post("/api/workspaces/{workspaceId}/projects/{projectId}/members", workspaceId, projectId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"));
}

}
