package com.akarengin.pulseforge.workspace.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.akarengin.pulseforge.workspace.dto.WorkspaceMembershipRequest;
import com.akarengin.pulseforge.common.entity.User;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.workspace.entity.WorkspaceMembership;
import com.akarengin.pulseforge.workspace.entity.WorkspaceRole;
import com.akarengin.pulseforge.workspace.mapper.WorkspaceMembershipMapper;
import com.akarengin.pulseforge.workspace.service.WorkspaceMembershipService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkspaceMembershipController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceMembershipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkspaceMembershipService workspaceMembershipService;

    @MockitoBean
    private WorkspaceMembershipMapper workspaceMembershipMapper;

    @Test
    void addMember_shouldReturn201() throws Exception {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        WorkspaceMembershipRequest request = new WorkspaceMembershipRequest(userId, WorkspaceRole.MEMBER);

        WorkspaceMembership membership = WorkspaceMembership.builder()
                .id(UUID.randomUUID())
                .workspace(Workspace.builder().id(workspaceId).build())
                .user(User.builder().id(userId).build())
                .role(WorkspaceRole.MEMBER)
                .build();

        when(workspaceMembershipService.addUserToWorkspace(any(UUID.class), any(UUID.class), any(WorkspaceRole.class))).thenReturn(membership);

        mockMvc.perform(post("/api/workspaces/{workspaceId}/members", workspaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getMembers_shouldReturn200() throws Exception {
        UUID workspaceId = UUID.randomUUID();

        when(workspaceMembershipService.getWorkspaceMembers(workspaceId)).thenReturn(List.of());

        mockMvc.perform(get("/api/workspaces/{workspaceId}/members", workspaceId))
                .andExpect(status().isOk());
    }
}
