package com.akarengin.pulseforge.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.entity.Project;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.exception.ResourceNotFoundException;
import com.akarengin.pulseforge.exception.ResourceNotFoundException;
import com.akarengin.pulseforge.mapper.EventMapper;
import com.akarengin.pulseforge.repository.EventRepository;
import com.akarengin.pulseforge.repository.ProjectRepository;
import com.akarengin.pulseforge.repository.WorkspaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final EventMapper eventMapper;

    @Transactional
    public Event createEvent(UUID workspaceId, UUID projectId, EventRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found for ID " + workspaceId));

        Project project = projectRepository.findByWorkspace_IdAndId(workspaceId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for ID " + projectId + " in workspace " + workspaceId));

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