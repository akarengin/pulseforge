package com.akarengin.pulseforge.service;

import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.repository.WorkspaceRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public Workspace createWorkspace(@NotBlank String name) {
        Workspace savedWorkspace = Workspace.builder()
            .name(name)
            .build();
        return workspaceRepository.save(savedWorkspace);
    }

    public Optional<Workspace> getWorkspaceById(UUID id) {
        return workspaceRepository.findById(id);
    }

    public Optional<Workspace> getWorkspaceByName(String name) {
        return workspaceRepository.findByName(name);
    }

}

