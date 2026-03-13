package com.akarengin.pulseforge.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.mapper.EventMapper;
import com.akarengin.pulseforge.repository.EventRepository;
import com.akarengin.pulseforge.repository.WorkspaceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    private final WorkspaceRepository workspaceRepository;

    private final EventMapper eventMapper;

    @Transactional
    public Event createEvent(UUID workspaceId, EventRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found for ID " + workspaceId));

        Event event = eventMapper.toEntity(request, workspace);
        return eventRepository.save(event);
    }

    public List<Event> getEventsByWorkspace(UUID workspaceId) {
        return eventRepository.findByWorkspace_Id(workspaceId);
    }

    public List<Event> getEventsByWorkspaceAndType(UUID workspaceId, String type) {
        return eventRepository.findByWorkspace_IdAndType(workspaceId, type);
    }

}