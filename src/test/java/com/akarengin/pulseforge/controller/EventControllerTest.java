package com.akarengin.pulseforge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false) // Disables Security Filters for unit testing
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Correctly injecting the Spring-managed ObjectMapper (handles Instant/Dates correctly)
    @Autowired
    private ObjectMapper objectMapper;

    // Correct replacement for @MockBean in Spring Boot 3.4+
    @MockitoBean
    private EventService eventService;

    @Test
    void createEvent_returns201AndBody() throws Exception {
        // Given
        var timestamp = Instant.parse("2026-01-01T00:00:00Z");

        var createdEvent = Event.builder()
            .id(1L)
            .type("user_login")
            .payload("{\"userId\":123,\"ip\":\"192.168.1.1\"}")
            .timestamp(timestamp)
            .build();

        when(eventService.createEvent(any(EventRequest.class))).thenReturn(createdEvent);

        var payloadMap = Map.of(
            "userId", 123,
            "ip", "192.168.1.1"
        );

        var request = new EventRequest(
            "user_login",
            objectMapper.writeValueAsString(payloadMap)
        );

        // When & Then
        mockMvc.perform(
                post("/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.type").value("user_login"))
            // Careful: this checks the payload string exactly.
            // If JSON spacing changes, this test fails. Consider checking structure if possible.
            .andExpect(jsonPath("$.payload").value("{\"userId\":123,\"ip\":\"192.168.1.1\"}"))
            .andExpect(jsonPath("$.timestamp").value("2026-01-01T00:00:00Z"));
    }

    @Test
    void createEvent_withBlankType_returns400() throws Exception {
        // Clean Text Block syntax (no escaping needed)
        var requestJson = """
                {
                  "type": "",
                  "payload": "{}"
                }
                """;

        mockMvc.perform(
                post("/events")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isBadRequest());
    }
}