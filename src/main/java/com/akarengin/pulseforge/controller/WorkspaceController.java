package com.akarengin.pulseforge.controller;

import com.akarengin.pulseforge.dto.WorkspaceRequestDTO;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.service.WorkspaceService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<Workspace> createWorkspace(@RequestBody @Valid WorkspaceRequestDTO request) {
        Workspace workspace = workspaceService.createWorkspace(request.name());

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(workspace.getId())
            .toUri();

        return ResponseEntity.created(location).body(workspace);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workspace> getWorkspace(@PathVariable UUID id) {
        Optional<Workspace> workspace = workspaceService.getWorkspaceById(id);
        return workspace.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}

