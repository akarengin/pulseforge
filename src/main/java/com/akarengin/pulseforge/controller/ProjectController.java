package com.akarengin.pulseforge.controller;

import com.akarengin.pulseforge.dto.ProjectRequest;
import com.akarengin.pulseforge.dto.ProjectResponse;
import com.akarengin.pulseforge.entity.Project;
import com.akarengin.pulseforge.mapper.ProjectMapper;
import com.akarengin.pulseforge.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@PathVariable UUID workspaceId,
                                                       @Valid @RequestBody ProjectRequest request) {
        Project project = projectService.createProject(workspaceId, request.getName());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(project.getId())
                .toUri();
        return ResponseEntity.created(location).body(projectMapper.toResponse(project));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(@PathVariable UUID workspaceId) {
        List<Project> projects = projectService.getProjectsByWorkspace(workspaceId);
        return ResponseEntity.ok(projectMapper.toResponseList(projects));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable UUID workspaceId,
                                                    @PathVariable UUID projectId) {
        Project project = projectService.getProject(workspaceId, projectId);
        return ResponseEntity.ok(projectMapper.toResponse(project));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID workspaceId,
                                            @PathVariable UUID projectId) {
        projectService.deleteProject(workspaceId, projectId);
        return ResponseEntity.noContent().build();
    }
}
