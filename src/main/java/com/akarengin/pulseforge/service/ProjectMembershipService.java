package com.akarengin.pulseforge.service;

import com.akarengin.pulseforge.entity.Project;
import com.akarengin.pulseforge.entity.ProjectMembership;
import com.akarengin.pulseforge.entity.ProjectRole;
import com.akarengin.pulseforge.entity.User;
import com.akarengin.pulseforge.exception.ResourceNotFoundException;
import com.akarengin.pulseforge.repository.ProjectMembershipRepository;
import com.akarengin.pulseforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectMembershipService {

    private final ProjectMembershipRepository projectMembershipRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final WorkspaceMembershipService workspaceMembershipService;

    @Transactional
    public ProjectMembership addUserToProject(UUID workspaceId, UUID projectId, UUID userId, ProjectRole role) {
        Project project = projectService.getProject(workspaceId, projectId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("User not found for ID {}", userId);
                return new ResourceNotFoundException("User not found for ID " + userId);
            });

        if (!workspaceMembershipService.isMember(workspaceId, userId)) {
            log.warn("User {} is not a member of workspace {}", userId, workspaceId);
            throw new IllegalStateException("User must be a member of the workspace to be added to a project");
        }

        projectMembershipRepository.findByProject_IdAndUser_Id(projectId, userId)
            .ifPresent(membership -> {
                log.warn("User {} is already a member of project {}", userId, projectId);
                throw new IllegalStateException("User already member of project");
            });

        ProjectMembership membership = ProjectMembership.builder()
            .project(project)
            .user(user)
            .role(role)
            .build();

        log.debug("Saving project membership {}", membership);
        return projectMembershipRepository.save(membership);
    }

    public List<ProjectMembership> getProjectMembers(UUID workspaceId, UUID projectId) {
        projectService.getProject(workspaceId, projectId);
        return projectMembershipRepository.findByProject_Id(projectId);
    }

    public boolean isMember(UUID projectId, UUID userId) {
        return projectMembershipRepository.findByProject_IdAndUser_Id(projectId, userId).isPresent();
    }

    public Optional<ProjectRole> getUserRole(UUID projectId, UUID userId) {
        return projectMembershipRepository.findByProject_IdAndUser_Id(projectId, userId)
            .map(ProjectMembership::getRole);
    }

    @Transactional
    public void removeUserFromProject(UUID workspaceId, UUID projectId, UUID userId) {
        projectService.getProject(workspaceId, projectId);
        
        ProjectMembership membership = projectMembershipRepository.findByProject_IdAndUser_Id(projectId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " is not a member of project " + projectId));
            
        projectMembershipRepository.delete(membership);
        log.info("Removed user {} from project {}", userId, projectId);
    }
}
