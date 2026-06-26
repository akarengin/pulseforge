package com.akarengin.pulseforge.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.akarengin.pulseforge.ingestion.dto.EventMessage;
import com.akarengin.pulseforge.ingestion.dto.EventRequest;
import com.akarengin.pulseforge.ingestion.entity.Event;
import com.akarengin.pulseforge.ingestion.repository.EventRepository;
import com.akarengin.pulseforge.ingestion.service.EventPersistenceService;
import com.akarengin.pulseforge.processing.EventConsumer;
import com.akarengin.pulseforge.project.entity.Project;
import com.akarengin.pulseforge.project.repository.ProjectRepository;
import com.akarengin.pulseforge.workspace.entity.Workspace;
import com.akarengin.pulseforge.workspace.repository.WorkspaceRepository;
import com.rabbitmq.client.Channel;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
class IdempotencyIntegrationTest {

    @Autowired
    private EventPersistenceService eventPersistenceService;

    @Autowired
    private EventConsumer eventConsumer;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @AfterEach
    void cleanup() {
        eventRepository.deleteAll();
        projectRepository.deleteAll();
        workspaceRepository.deleteAll();
    }

    @Test
    void brokerRedelivery_duplicateIdempotencyKey_swallowedSilently() throws Exception {
        // Arrange: create workspace + project
        Workspace ws = workspaceRepository.save(Workspace.builder().name("Test").build());
        Project project = projectRepository.save(Project.builder().workspace(ws).name("P1").build());

        // Arrange: create an EventMessage with a known idempotencyKey
        String idempotencyKey = "dedup-key-" + UUID.randomUUID();
        EventMessage eventMessage = new EventMessage(
            "click",
            Map.of("button", "submit"),
            ws.getId(),
            project.getId(),
            idempotencyKey
        );

        // Act: persist the event twice (simulating broker redelivery)
        long deliveryTag = 1L;
        Channel channel = mock(Channel.class);
        Message message = mock(Message.class);
        MessageProperties messageProperties = mock(MessageProperties.class);
        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(deliveryTag);

        eventConsumer.consumeEvent(eventMessage, message, channel);
        eventConsumer.consumeEvent(eventMessage, message, channel);

        // Arrange: create EventRequest for direct service call
        EventRequest request = new EventRequest("click", Map.of("button", "submit"), idempotencyKey);

        // Assert: first call saved successfully (verify one event exists after first consumption)
        List<Event> afterFirstCall = eventRepository.findByWorkspace_IdAndProject_Id(ws.getId(), project.getId());
        assertThat(afterFirstCall).hasSize(1);

        // Second call throws — the REQUIRES_NEW transaction rolls back cleanly
        assertThatThrownBy(() -> eventPersistenceService.persist(ws.getId(), project.getId(), request))
            .isInstanceOf(DataIntegrityViolationException.class);

        verify(channel, times(2)).basicAck(deliveryTag, false);
        verify(channel, never()).basicNack(deliveryTag, false, false);

        // Assert: only ONE row in the DB for this workspace
        List<Event> events = eventRepository.findByWorkspace_IdAndProject_Id(ws.getId(), project.getId());
        assertThat(events).hasSize(1);
    }

    @Test
    void differentIdempotencyKeys_bothPersisted() throws Exception {
        // Arrange
        Workspace ws = workspaceRepository.save(Workspace.builder().name("Test").build());
        Project project = projectRepository.save(Project.builder().workspace(ws).name("P1").build());

        EventRequest request1 = new EventRequest("click", Map.of("a", "1"), "key-1");
        EventRequest request2 = new EventRequest("click", Map.of("a", "2"), "key-2");

        // Act
        Event first = eventPersistenceService.persist(ws.getId(), project.getId(), request1);
        Event second = eventPersistenceService.persist(ws.getId(), project.getId(), request2);

        // Assert: both persisted
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(eventRepository.findByWorkspace_IdAndProject_Id(ws.getId(), project.getId())).hasSize(2);
    }

    @Test
    void sameKeyDifferentWorkspaces_bothPersisted() throws Exception {
        // Arrange: two workspaces
        Workspace ws1 = workspaceRepository.save(Workspace.builder().name("Tenant A").build());
        Workspace ws2 = workspaceRepository.save(Workspace.builder().name("Tenant B").build());
        Project p1 = projectRepository.save(Project.builder().workspace(ws1).name("P1").build());
        Project p2 = projectRepository.save(Project.builder().workspace(ws2).name("P2").build());

        String sharedKey = "same-key-across-tenants";
        EventRequest req = new EventRequest("click", Map.of("x", "1"), sharedKey);

        // Act: persist with same key in different workspaces
        Event e1 = eventPersistenceService.persist(ws1.getId(), p1.getId(), req);
        Event e2 = eventPersistenceService.persist(ws2.getId(), p2.getId(), req);

        // Assert: both saved — constraint is per-workspace, not global
        assertThat(e1).isNotNull();
        assertThat(e2).isNotNull();
    }
}
