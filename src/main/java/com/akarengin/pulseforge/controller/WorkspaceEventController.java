package com.akarengin.pulseforge.controller;

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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.dto.EventResponse;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.mapper.EventMapper;
import com.akarengin.pulseforge.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/events")
@RequiredArgsConstructor
public class WorkspaceEventController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@PathVariable UUID workspaceId,
                                             @Valid @RequestBody EventRequest request) {
        Event event = eventService.createEvent(workspaceId, request);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(event.getId())
            .toUri();
        return ResponseEntity.created(location).body(eventMapper.toResponse(event));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents(@PathVariable UUID workspaceId) {
        List<Event> eventsByWorkspace = eventService.getEventsByWorkspace(workspaceId);
        return ResponseEntity.ok(eventMapper.toResponseList(eventsByWorkspace));
    }

    @GetMapping(params = "type")
    public ResponseEntity<List<EventResponse>> getEventsByType(@PathVariable UUID workspaceId,
                                                       @RequestParam String type) {
        List<Event> eventsByWorkspaceAndType = eventService.getEventsByWorkspaceAndType(workspaceId,
            type);
        return ResponseEntity.ok(eventMapper.toResponseList(eventsByWorkspaceAndType));
    }

}
