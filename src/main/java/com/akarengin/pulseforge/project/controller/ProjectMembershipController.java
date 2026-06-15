package com.akarengin.pulseforge.project.controller;

import com.akarengin.pulseforge.project.dto.ProjectMembershipRequest;
import com.akarengin.pulseforge.project.dto.ProjectMembershipResponse;
import com.akarengin.pulseforge.project.entity.ProjectMembership;
import com.akarengin.pulseforge.project.mapper.ProjectMembershipMapper;
import com.akarengin.pulseforge.project.service.ProjectMembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMembershipController {

    private final ProjectMembershipService projectMembershipService;
    private final ProjectMembershipMapper projectMembershipMapper;

    @PostMapping
    public ResponseEntity<ProjectMembershipResponse> addMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectMembershipRequest request) {

        ProjectMembership membership = projectMembershipService.addUserToProject(
                workspaceId, projectId, request.userId(), request.role()
        );

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{userId}")
            .buildAndExpand(membership.getUser().getId())
            .toUri();

        return ResponseEntity.created(location).body(projectMembershipMapper.toResponse(membership));
    }

    @GetMapping
    public ResponseEntity<List<ProjectMembershipResponse>> getMembers(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId) {

        List<ProjectMembership> members = projectMembershipService.getProjectMembers(workspaceId, projectId);
        return ResponseEntity.ok(projectMembershipMapper.toResponseList(members));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID workspaceId,
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {

        projectMembershipService.removeUserFromProject(workspaceId, projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
