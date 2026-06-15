package com.akarengin.pulseforge.workspace.service;

import com.akarengin.pulseforge.workspace.entity.WorkspaceMembership;
import com.akarengin.pulseforge.workspace.entity.WorkspaceRole;
import com.akarengin.pulseforge.common.entity.User;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.common.exception.ResourceNotFoundException;
import com.akarengin.pulseforge.workspace.repository.WorkspaceMembershipRepository;
import com.akarengin.pulseforge.user.service.UserService;
import com.akarengin.pulseforge.workspace.repository.WorkspaceRepository;
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
public class WorkspaceMembershipService {

    private final WorkspaceMembershipRepository workspaceMembershipRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserService userService;

    @Transactional
    public WorkspaceMembership addUserToWorkspace(UUID workspaceId, UUID userId, WorkspaceRole role) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> {
                log.warn("Workspace not found for ID {}", workspaceId);
                return new ResourceNotFoundException("Workspace not found for ID " + workspaceId);
            });

        User user = userService.getUserById(userId);

        workspaceMembershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)
            .ifPresent(membership -> {
                log.warn("User {} is already a member of workspace {}", userId, workspaceId);
                throw new IllegalStateException("User already member of workspace");
            });

        WorkspaceMembership membership = WorkspaceMembership.builder()
            .workspace(workspace)
            .user(user)
            .role(role)
            .build();

        log.debug("Saving workspace membership {}", membership);
        return workspaceMembershipRepository.save(membership);
    }

    public List<WorkspaceMembership> getWorkspaceMembers(UUID workspaceId) {
        return workspaceMembershipRepository.findByWorkspace_Id(workspaceId);
    }

    public boolean isMember(UUID workspaceId, UUID userId) {
        return workspaceMembershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId).isPresent();
    }

    public Optional<WorkspaceRole> getUserRole(UUID workspaceId, UUID userId) {
        return workspaceMembershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)
            .map(WorkspaceMembership::getRole);
    }

}
