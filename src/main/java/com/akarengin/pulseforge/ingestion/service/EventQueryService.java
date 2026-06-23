package com.akarengin.pulseforge.ingestion.service;

import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.ingestion.repository.EventRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventQueryService {

    private final EventRepository eventRepository;

    public List<Event> getEventsByWorkspaceAndProject(UUID workspaceId, UUID projectId) {
        return eventRepository.findByWorkspace_IdAndProject_Id(workspaceId, projectId);
    }

    public List<Event> getEventsByWorkspaceAndProjectAndType(UUID workspaceId, UUID projectId, String type) {
        return eventRepository.findByWorkspace_IdAndProject_IdAndType(workspaceId, projectId, type);
    }
}
