package com.akarengin.pulseforge.project.service;

import com.akarengin.pulseforge.common.entity.*;
import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.project.entity.ProjectMembership;
import com.akarengin.pulseforge.project.entity.ProjectRole;
import com.akarengin.pulseforge.project.repository.ProjectMembershipRepository;
import com.akarengin.pulseforge.user.service.UserService;
import com.akarengin.pulseforge.workspace.service.WorkspaceMembershipService;
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
class ProjectMembershipServiceTest {

    @Mock
    private ProjectMembershipRepository projectMembershipRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private WorkspaceMembershipService workspaceMembershipService;

    @InjectMocks
    private ProjectMembershipService projectMembershipService;

    @Test
    void testAddUserToProject() {
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Project project = Project.builder().id(projectId).build();
        User user = User.builder().id(userId).build();
        ProjectMembership membership = ProjectMembership.builder()
            .project(project)
            .user(user)
            .role(ProjectRole.CONTRIBUTOR)
            .build();

        when(projectService.getProject(workspaceId, projectId)).thenReturn(project);
        when(userService.getUserById(userId)).thenReturn(user);
        when(workspaceMembershipService.isMember(workspaceId, userId)).thenReturn(true);
        when(projectMembershipRepository.findByProject_IdAndUser_Id(projectId, userId)).thenReturn(Optional.empty());
        when(projectMembershipRepository.save(any())).thenReturn(membership);

        ProjectMembership result = projectMembershipService.addUserToProject(workspaceId, projectId, userId, ProjectRole.CONTRIBUTOR);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(ProjectRole.CONTRIBUTOR);
    }
}
