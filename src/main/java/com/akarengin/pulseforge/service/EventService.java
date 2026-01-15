package com.akarengin.pulseforge.service;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.repository.EventRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Event createEvent(@Valid @NotNull EventRequest request) {
        try {
            Event event = Event.builder()
                .type(request.type())
                .payload(objectMapper.writeValueAsString(request.payload()))
                .build();
            return eventRepository.save(event);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid payload format", e);
        }
    }
}
