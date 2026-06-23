package com.akarengin.pulseforge.ingestion.service;

import com.akarengin.pulseforge.common.exception.ResourceNotFoundException;
import com.akarengin.pulseforge.ingestion.dto.EventRequest;
import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.ingestion.mapper.EventMapper;
import com.akarengin.pulseforge.ingestion.repository.EventRepository;
import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.project.service.ProjectService;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.workspace.service.WorkspaceService;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventPersistenceService {

    private final EventRepository eventRepository;
    private final WorkspaceService workspaceService;
    private final ProjectService projectService;
    private final EventMapper eventMapper;

    @Transactional
    public Event persist(UUID workspaceId, UUID projectId, EventRequest request) throws IOException {
        Workspace workspace = workspaceService.getWorkspaceById(workspaceId)
            .orElseThrow(() -> new ResourceNotFoundException("Workspace not found for ID " + workspaceId));

        Project project = projectService.getProject(workspaceId, projectId);

        Event event = eventMapper.toEntity(request, workspace, project);
        return eventRepository.save(event);
    }
}
