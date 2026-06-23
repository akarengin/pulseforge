package com.akarengin.pulseforge.ingestion.controller;

import com.akarengin.pulseforge.ingestion.dto.EventMessage;
import com.akarengin.pulseforge.ingestion.service.EventPublisher;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.akarengin.pulseforge.ingestion.dto.EventRequest;
import com.akarengin.pulseforge.ingestion.dto.EventResponse;
import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.ingestion.mapper.EventMapper;
import com.akarengin.pulseforge.ingestion.service.EventQueryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/projects/{projectId}/events")
@RequiredArgsConstructor
public class EventIngestionController {

    private final EventQueryService eventQueryService;
    private final EventMapper eventMapper;
    private final EventPublisher eventPublisher;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@PathVariable UUID workspaceId,
                                             @PathVariable UUID projectId,
                                             @Valid @RequestBody EventRequest request) {
        EventMessage eventMessage = new EventMessage(request.type(), request.payload(), workspaceId, projectId);
        eventPublisher.publishEvent(eventMessage);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents(@PathVariable UUID workspaceId,
                                                 @PathVariable UUID projectId) {
        List<Event> events = eventQueryService.getEventsByWorkspaceAndProject(workspaceId, projectId);
        return ResponseEntity.ok(eventMapper.toResponseList(events));
    }

    @GetMapping(params = "type")
    public ResponseEntity<List<EventResponse>> getEventsByType(@PathVariable UUID workspaceId,
                                                       @PathVariable UUID projectId,
                                                       @RequestParam String type) {
        List<Event> events = eventQueryService.getEventsByWorkspaceAndProjectAndType(workspaceId, projectId, type);
        return ResponseEntity.ok(eventMapper.toResponseList(events));
    }

}
