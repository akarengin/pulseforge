package com.akarengin.pulseforge.service;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    @Test
    void createEvent_buildsAndSavesEvent() {
        EventRequest request = new EventRequest("user_login", "{\"userId\":123}");

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(request);

        // Verify what was passed to the repository
        verify(eventRepository).save(eventCaptor.capture());
        Event saved = eventCaptor.getValue();

        assertThat(saved.getType()).isEqualTo("user_login");
        assertThat(saved.getPayload()).isEqualTo("{\"userId\":123}");

        // Verify service returns what repository returns
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void createEvent_whenRepositoryFails_throwsException() {
        EventRequest request = new EventRequest("user_login", "{\"userId\":123}");

        when(eventRepository.save(any(Event.class))).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> eventService.createEvent(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");
    }

}
