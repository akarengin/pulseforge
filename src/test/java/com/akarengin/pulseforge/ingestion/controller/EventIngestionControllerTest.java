package com.akarengin.pulseforge.ingestion.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.akarengin.pulseforge.ingestion.dto.EventRequest;
import com.akarengin.pulseforge.ingestion.dto.EventResponse;
import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.ingestion.mapper.EventMapper;
import com.akarengin.pulseforge.ingestion.service.EventPublisher;
import com.akarengin.pulseforge.ingestion.service.EventQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(EventIngestionController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventIngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private EventQueryService eventQueryService;

    @MockitoBean
    private EventPublisher eventPublisher;

    @MockitoBean
    private EventMapper eventMapper;

    @Test
    void createEvent_returns202AndPublishesMessage() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var projectId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var request = new EventRequest("user_login", Map.of("userId", 123), "random_uuid");

        mockMvc.perform(
                post("/api/workspaces/" + workspaceId + "/projects/" + projectId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(eventPublisher).publishEvent(argThat(eventMessage ->
                eventMessage.workspaceId().equals(workspaceId)
                        && eventMessage.projectId().equals(projectId)
                        && eventMessage.type().equals("user_login")
                        && eventMessage.payload().equals(Map.of("userId", 123))));
    }

    @Test
    void createEvent_withBlankType_returns400() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var projectId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var requestJson = """
                {
                  "type": "",
                  "payload": {}
                }
                """;

        mockMvc.perform(
                post("/api/workspaces/" + workspaceId + "/projects/" + projectId + "/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEvents_returns200AndList() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var projectId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var e1 = Event.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).type("a").payload(Map.of())
                .timestamp(Instant.parse("2026-01-01T00:00:00Z")).build();
        var e2 = Event.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002")).type("b").payload(Map.of())
                .timestamp(Instant.parse("2026-01-01T00:00:00Z")).build();

        var resp1 = new EventResponse(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        workspaceId,
                        projectId,
                        "a",
                        Map.of(),
                        Instant.parse("2026-01-01T00:00:00Z"));
        var resp2 = new EventResponse(
                        UUID.fromString("00000000-0000-0000-0000-000000000002"),
                        workspaceId,
                        projectId,
                        "b",
                        Map.of(),
                        Instant.parse("2026-01-01T00:00:00Z"));
        
        when(eventQueryService.getEventsByWorkspaceAndProject(workspaceId, projectId)).thenReturn(List.of(e1, e2));
        when(eventMapper.toResponseList(any())).thenReturn(List.of(resp1, resp2));

        mockMvc.perform(get("/api/workspaces/" + workspaceId + "/projects/" + projectId + "/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(e1.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(e2.getId().toString()));
    }

    @Test
    void getEventsByType_returns200AndList() throws Exception {
        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var projectId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        var e = Event.builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).type("user_login")
                .payload(Map.of()).timestamp(Instant.parse("2026-01-01T00:00:00Z")).build();

        var resp1 = new EventResponse(
                        UUID.fromString("00000000-0000-0000-0000-000000000001"),
                        workspaceId,
                        projectId,
                        "user_login",
                        Map.of(),
                        Instant.parse("2026-01-01T00:00:00Z"));

        when(eventQueryService.getEventsByWorkspaceAndProjectAndType(workspaceId, projectId, "user_login")).thenReturn(List.of(e));
        when(eventMapper.toResponseList(any())).thenReturn(List.of(resp1));

        mockMvc.perform(get("/api/workspaces/" + workspaceId + "/projects/" + projectId + "/events").param("type", "user_login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(e.getId().toString()))
                .andExpect(jsonPath("$[0].type").value("user_login"));
    }
}
