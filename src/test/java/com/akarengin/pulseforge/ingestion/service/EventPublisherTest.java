package com.akarengin.pulseforge.ingestion.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.akarengin.pulseforge.common.exception.ResourceNotFoundException;
import com.akarengin.pulseforge.ingestion.config.RabbitConfig;
import com.akarengin.pulseforge.ingestion.dto.EventMessage;
import com.akarengin.pulseforge.project.service.ProjectService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private ProjectService projectService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventPublisher eventPublisher;

    private EventMessage message;

    @BeforeEach
    void setUp() {
        message = new EventMessage(
            "click", Map.of(), UUID.randomUUID(), UUID.randomUUID(), "key-1"
        );
    }

    @Test
    void newKey_publishesToBroker() {
        when(idempotencyService.checkAndSet(message.workspaceId(), message.idempotencyKey()))
            .thenReturn(true);

        eventPublisher.publishEvent(message);

        verify(rabbitTemplate).convertAndSend(
            RabbitConfig.EVENTS_EXCHANGE, RabbitConfig.EVENTS_ROUTING_KEY, message
        );
    }

    @Test
    void duplicateKey_doesNotPublish() {
        when(idempotencyService.checkAndSet(message.workspaceId(), message.idempotencyKey()))
            .thenReturn(false);

        eventPublisher.publishEvent(message);

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void redisFailure_failOpen_publishesToBroker() {
        when(idempotencyService.checkAndSet(any(), any()))
            .thenThrow(new RuntimeException("Redis unavailable"));

        eventPublisher.publishEvent(message);

        verify(rabbitTemplate).convertAndSend(
            RabbitConfig.EVENTS_EXCHANGE, RabbitConfig.EVENTS_ROUTING_KEY, message
        );
    }

    @Test
    void projectNotFound_throws_doesNotPublish() {
        doThrow(new ResourceNotFoundException("Project not found"))
            .when(projectService).getProject(message.workspaceId(), message.projectId());

        assertThatThrownBy(() -> eventPublisher.publishEvent(message))
            .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(rabbitTemplate);
        verifyNoInteractions(idempotencyService);
    }
}
