package com.akarengin.pulseforge.service;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    @Transactional
    public Event createEvent(EventRequest request) {
        Event event = Event.builder()
            .type(request.type())
            .payload(request.payload())
            .build();

        return eventRepository.save(event);
    }

}