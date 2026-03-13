package com.akarengin.pulseforge.service;

import com.akarengin.pulseforge.entity.Membership;
import com.akarengin.pulseforge.entity.User;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.repository.MembershipRepository;
import com.akarengin.pulseforge.repository.UserRepository;
import com.akarengin.pulseforge.repository.WorkspaceRepository;
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
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Transactional
    public Membership addUserToWorkspace(UUID workspaceId, UUID userId, String role) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
            .orElseThrow(() -> {
                log.warn("Workspace not found for ID {}", workspaceId);
                return new IllegalArgumentException("Workspace not found for ID " + workspaceId);
            });

        User user = userRepository.findById(userId)
            .orElseThrow(() -> {
                log.warn("User not found for ID {}", userId);
                return new IllegalArgumentException("User not found for ID " + userId);
            });

        membershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)
            .ifPresent(membership -> {
                log.warn("User {} is already a member of workspace {}", userId, workspaceId);
                throw new IllegalArgumentException("User already member of workspace");
            });

        Membership membership = Membership.builder()
            .workspace(workspace)
            .user(user)
            .role(role)
            .build();

        log.debug("Saving membership {}", membership);
        return membershipRepository.save(membership);
    }

    public List<Membership> getWorkspaceMembers(UUID workspaceId) {
        return membershipRepository.findByWorkspace_Id(workspaceId);
    }

    public boolean isMember(UUID workspaceId, UUID userId) {
        return membershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId).isPresent();
    }

    public Optional<String> getUserRole(UUID workspaceId, UUID userId) {
        return membershipRepository.findByWorkspace_IdAndUser_Id(workspaceId, userId)
            .map(Membership::getRole);
    }

}

