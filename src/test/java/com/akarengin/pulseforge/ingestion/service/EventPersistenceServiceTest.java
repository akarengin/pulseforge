package com.akarengin.pulseforge.ingestion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.akarengin.pulseforge.ingestion.dto.EventRequest;
import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.ingestion.mapper.EventMapper;
import com.akarengin.pulseforge.ingestion.repository.EventRepository;
import com.akarengin.pulseforge.project.service.ProjectService;
import com.akarengin.pulseforge.workspace.service.WorkspaceService;
import java.io.IOException;
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
class EventPersistenceServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private ProjectService projectService;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventPersistenceService eventPersistenceService;

    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    @Test
    void persist_buildsAndSavesEvent() throws IOException {
        EventRequest request = new EventRequest("user_login", Map.of("userId", 123), "dedup-key-test");

        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var projectId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        
        Workspace workspace = Workspace.builder().id(workspaceId).build();
        Project project = Project.builder().id(projectId).build();
        
        when(workspaceService.getWorkspaceById(workspaceId)).thenReturn(Optional.of(workspace));
        when(projectService.getProject(workspaceId, projectId)).thenReturn(project);
        
        Event mappedEvent = Event.builder().type("user_login").payload(Map.of("userId", 123)).build();
        when(eventMapper.toEntity(eq(request), any(Workspace.class), any(Project.class))).thenReturn(mappedEvent);
        when(eventRepository.saveAndFlush(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventPersistenceService.persist(workspaceId, projectId, request);

        verify(eventRepository).saveAndFlush(eventCaptor.capture());
        Event saved = eventCaptor.getValue();

        assertThat(saved.getType()).isEqualTo("user_login");
        assertThat(saved.getPayload()).isEqualTo(Map.of("userId", 123));
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void persist_whenRepositoryFails_throwsException() throws IOException {
        EventRequest request = new EventRequest("user_login", Map.of("userId", 123), "dedup-key-test");

        var workspaceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var projectId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        
        Workspace workspace = Workspace.builder().id(workspaceId).build();
        Project project = Project.builder().id(projectId).build();
        
        when(workspaceService.getWorkspaceById(workspaceId)).thenReturn(Optional.of(workspace));
        when(projectService.getProject(workspaceId, projectId)).thenReturn(project);
        
        Event mappedEvent = Event.builder().type("user_login").payload(Map.of("userId", 123)).build();
        when(eventMapper.toEntity(eq(request), any(Workspace.class), any(Project.class))).thenReturn(mappedEvent);
        when(eventRepository.saveAndFlush(any(Event.class))).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> eventPersistenceService.persist(workspaceId, projectId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }

}
