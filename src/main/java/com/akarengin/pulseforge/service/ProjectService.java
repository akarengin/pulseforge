package com.akarengin.pulseforge.service;

import com.akarengin.pulseforge.entity.Project;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.repository.ProjectRepository;
import com.akarengin.pulseforge.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public Project createProject(UUID workspaceId, String name) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found: " + workspaceId));

        if (projectRepository.existsByWorkspace_IdAndName(workspaceId, name)) {
            throw new IllegalArgumentException("Project with name '" + name + "' already exists in this workspace");
        }

        Project project = Project.builder()
                .workspace(workspace)
                .name(name)
                .build();

        log.info("Creating project '{}' in workspace '{}'", name, workspaceId);
        return projectRepository.save(project);
    }

    public List<Project> getProjectsByWorkspace(UUID workspaceId) {
        return projectRepository.findByWorkspace_Id(workspaceId);
    }

    public Project getProject(UUID workspaceId, UUID projectId) {
        return projectRepository.findByWorkspace_IdAndId(workspaceId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId + " in workspace " + workspaceId));
    }

    @Transactional
    public void deleteProject(UUID workspaceId, UUID projectId) {
        Project project = getProject(workspaceId, projectId);
        projectRepository.delete(project);
        log.info("Deleted project '{}' from workspace '{}'", projectId, workspaceId);
    }
}
