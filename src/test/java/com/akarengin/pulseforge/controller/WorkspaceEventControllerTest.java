package com.akarengin.pulseforge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.dto.EventResponse;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.mapper.EventMapper;
import com.akarengin.pulseforge.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(WorkspaceEventController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EventMapper eventMapper;

    @Test
    void createEvent_returns201_setsLocation_andBody() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var eventId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        var created = Event.builder()
                .id(eventId)
                .type("user_login")
                .payload(Map.of("userId", 123))
                .timestamp(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
                
        // Use manual construction
        var expectedResponse = new EventResponse(
                        eventId,
                        workspaceId,
                        "user_login",
                        Map.of("userId", 123),
                        Instant.parse("2026-01-01T00:00:00Z"));
        
        when(eventService.createEvent(eq(workspaceId), any(EventRequest.class))).thenReturn(created);
        when(eventMapper.toResponse(any(Event.class))).thenReturn(expectedResponse);

        var request = new EventRequest("user_login", Map.of("userId", 123));

        mockMvc.perform(
                post("/api/workspaces/" + workspaceId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "http://localhost/api/workspaces/" + workspaceId + "/events/" + eventId))
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.type").value("user_login"));
    }

    @Test
    void createEvent_withBlankType_returns400() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var requestJson = """
                {
                  "type": "",
                  "payload": {}
                }
                """;

        mockMvc.perform(
                post("/api/workspaces/" + workspaceId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEvents_returns200AndList() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var e1 = Event.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).type("a").payload(Map.of())
                .timestamp(Instant.parse("2026-01-01T00:00:00Z")).build();
        var e2 = Event.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002")).type("b").payload(Map.of())
                .timestamp(Instant.parse("2026-01-01T00:00:00Z")).build();

        // Use manual construction
        var resp1 = new EventResponse(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        workspaceId,
                        "a",
                        Map.of(),
                        Instant.parse("2026-01-01T00:00:00Z"));
        var resp2 = new EventResponse(
                        UUID.fromString("00000000-0000-0000-0000-000000000002"),
                        workspaceId,
                        "b",
                        Map.of(),
                        Instant.parse("2026-01-01T00:00:00Z"));
        
        when(eventService.getEventsByWorkspace(workspaceId)).thenReturn(List.of(e1, e2));
        when(eventMapper.toResponseList(any())).thenReturn(List.of(resp1, resp2));

        mockMvc.perform(get("/api/workspaces/" + workspaceId + "/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(e1.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(e2.getId().toString()));
    }

    @Test
    void getEventsByType_returns200AndList() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var e = Event.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).type("user_login")
                .payload(Map.of()).timestamp(Instant.parse("2026-01-01T00:00:00Z")).build();

        // Use manual construction
        var resp1 = new EventResponse(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        workspaceId,
                        "user_login",
                        Map.of(),
                        Instant.parse("2026-01-01T00:00:00Z"));

        when(eventService.getEventsByWorkspaceAndType(workspaceId, "user_login")).thenReturn(List.of(e));
        when(eventMapper.toResponseList(any())).thenReturn(List.of(resp1));

        mockMvc.perform(get("/api/workspaces/" + workspaceId + "/events").param("type", "user_login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(e.getId().toString()))
                .andExpect(jsonPath("$[0].type").value("user_login"));
    }
}
