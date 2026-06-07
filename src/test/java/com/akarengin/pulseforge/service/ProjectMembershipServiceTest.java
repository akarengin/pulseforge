package com.akarengin.pulseforge.service;

import com.akarengin.pulseforge.entity.*;
import com.akarengin.pulseforge.repository.ProjectMembershipRepository;
import com.akarengin.pulseforge.repository.UserRepository;
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
    private UserRepository userRepository;

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
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(workspaceMembershipService.isMember(workspaceId, userId)).thenReturn(true);
        when(projectMembershipRepository.findByProject_IdAndUser_Id(projectId, userId)).thenReturn(Optional.empty());
        when(projectMembershipRepository.save(any())).thenReturn(membership);

        ProjectMembership result = projectMembershipService.addUserToProject(workspaceId, projectId, userId, ProjectRole.CONTRIBUTOR);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(ProjectRole.CONTRIBUTOR);
    }
}
