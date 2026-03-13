package com.akarengin.pulseforge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.akarengin.pulseforge.dto.EventRequest;
import com.akarengin.pulseforge.entity.Event;
import com.akarengin.pulseforge.entity.Workspace;
import com.akarengin.pulseforge.mapper.EventMapper;
import com.akarengin.pulseforge.repository.EventRepository;
import com.akarengin.pulseforge.repository.WorkspaceRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventService eventService;

    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    @Test
    void createEvent_buildsAndSavesEvent() {
        EventRequest request = new EventRequest("user_login", Map.of("userId", 123));

        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(Workspace.builder().id(workspaceId).build()));
        Event mappedEvent = Event.builder().type("user_login").payload(Map.of("userId", 123)).build();
        when(eventMapper.toEntity(eq(request), any(Workspace.class))).thenReturn(mappedEvent);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(workspaceId, request);

        // Verify what was passed to the repository
        verify(eventRepository).save(eventCaptor.capture());
        Event saved = eventCaptor.getValue();

        assertThat(saved.getType()).isEqualTo("user_login");
        assertThat(saved.getPayload()).isEqualTo(Map.of("userId", 123));

        // Verify service returns what repository returns
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void createEvent_whenRepositoryFails_throwsException() {
        EventRequest request = new EventRequest("user_login", Map.of("userId", 123));

        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(workspaceRepository.findById(workspaceId))
                .thenReturn(Optional.of(Workspace.builder().id(workspaceId).build()));
        Event mappedEvent = Event.builder().type("user_login").payload(Map.of("userId", 123)).build();
        when(eventMapper.toEntity(eq(request), any(Workspace.class))).thenReturn(mappedEvent);

        when(eventRepository.save(any(Event.class))).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> eventService.createEvent(workspaceId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

}
