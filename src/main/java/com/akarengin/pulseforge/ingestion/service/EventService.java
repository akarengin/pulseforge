package com.akarengin.pulseforge.ingestion.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.akarengin.pulseforge.ingestion.dto.EventRequest;
import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.common.exception.ResourceNotFoundException;
import com.akarengin.pulseforge.ingestion.mapper.EventMapper;
import com.akarengin.pulseforge.ingestion.repository.EventRepository;
import com.akarengin.pulseforge.project.service.ProjectService;
import com.akarengin.pulseforge.workspace.service.WorkspaceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final WorkspaceService workspaceService;
    private final ProjectService projectService;
    private final EventMapper eventMapper;

    @Transactional
    public Event createEvent(UUID workspaceId, UUID projectId, EventRequest request) {
        Workspace workspace = workspaceService.getWorkspaceById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found for ID " + workspaceId));

        Project project = projectService.getProject(workspaceId, projectId);

        Event event = eventMapper.toEntity(request, workspace, project);
        return eventRepository.save(event);
    }

    public List<Event> getEventsByWorkspaceAndProject(UUID workspaceId, UUID projectId) {
        return eventRepository.findByWorkspace_IdAndProject_Id(workspaceId, projectId);
    }

    public List<Event> getEventsByWorkspaceAndProjectAndType(UUID workspaceId, UUID projectId, String type) {
        return eventRepository.findByWorkspace_IdAndProject_IdAndType(workspaceId, projectId, type);
    }

}