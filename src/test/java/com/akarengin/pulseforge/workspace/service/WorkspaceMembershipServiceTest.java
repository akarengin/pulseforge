package com.akarengin.pulseforge.workspace.service;

import com.akarengin.pulseforge.common.entity.User;
import com.akarengin.pulseforge.user.repository.UserRepository;
import com.akarengin.pulseforge.user.service.UserService;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.workspace.entity.WorkspaceMembership;
import com.akarengin.pulseforge.workspace.entity.WorkspaceRole;
import com.akarengin.pulseforge.workspace.repository.WorkspaceMembershipRepository;
import com.akarengin.pulseforge.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceMembershipServiceTest {

    @Mock
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private com.akarengin.pulseforge.workspace.service.WorkspaceMembershipService workspaceMembershipService;

    @Test
    void testAddUserToWorkspace() {
        UUID workspaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Workspace workspace = Workspace.builder().id(workspaceId).name("Test").build();
        User user = User.builder().id(userId).email("test@example.com").build();
        WorkspaceMembership membership = WorkspaceMembership.builder()
            .workspace(workspace)
            .user(user)
            .role(WorkspaceRole.MEMBER)
            .build();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userService.getUserById(userId)).thenReturn(user);
        when(workspaceMembershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)).thenReturn(Optional.empty());
        when(workspaceMembershipRepository.save(any())).thenReturn(membership);

        WorkspaceMembership result = workspaceMembershipService.addUserToWorkspace(workspaceId, userId, WorkspaceRole.MEMBER);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(WorkspaceRole.MEMBER);
    }
}
