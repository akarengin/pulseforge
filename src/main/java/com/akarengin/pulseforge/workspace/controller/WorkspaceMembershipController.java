package com.akarengin.pulseforge.workspace.controller;

import com.akarengin.pulseforge.workspace.dto.WorkspaceMembershipRequest;
import com.akarengin.pulseforge.workspace.dto.WorkspaceMembershipResponse;
import com.akarengin.pulseforge.workspace.entity.WorkspaceMembership;
import com.akarengin.pulseforge.workspace.mapper.WorkspaceMembershipMapper;
import com.akarengin.pulseforge.workspace.service.WorkspaceMembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class WorkspaceMembershipController {

    private final WorkspaceMembershipService workspaceMembershipService;
    private final WorkspaceMembershipMapper workspaceMembershipMapper;

    @PostMapping
    public ResponseEntity<WorkspaceMembershipResponse> addMember(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody WorkspaceMembershipRequest request) {

        WorkspaceMembership membership = workspaceMembershipService.addUserToWorkspace(
                workspaceId, request.userId(), request.role()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{userId}")
                .buildAndExpand(membership.getUser().getId())
                .toUri();

        return ResponseEntity.created(location).body(workspaceMembershipMapper.toResponse(membership));
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceMembershipResponse>> getMembers(@PathVariable UUID workspaceId) {
        List<WorkspaceMembership> members = workspaceMembershipService.getWorkspaceMembers(workspaceId);
        return ResponseEntity.ok(workspaceMembershipMapper.toResponseList(members));
    }
}
