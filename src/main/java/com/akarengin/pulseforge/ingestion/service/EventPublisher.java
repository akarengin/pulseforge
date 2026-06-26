package com.akarengin.pulseforge.ingestion.service;

import com.akarengin.pulseforge.ingestion.config.RabbitConfig;
import com.akarengin.pulseforge.ingestion.dto.EventMessage;
import com.akarengin.pulseforge.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final IdempotencyService idempotencyService;
    private final ProjectService projectService;
    private final RabbitTemplate rabbitTemplate;

    public void publishEvent(EventMessage eventMessage) {
        log.info("Publishing event to queue: workspaceId={}, type={}", eventMessage.workspaceId(),
            eventMessage.type());

        // Validates that the project exists and belongs to the workspace.
        projectService.getProject(eventMessage.workspaceId(), eventMessage.projectId());

        try {
            if (!idempotencyService.checkAndSet(eventMessage.workspaceId(),
                eventMessage.idempotencyKey())) {
                log.info("Key already exists: workspaceId={}, idempotencyKey={}",
                    eventMessage.workspaceId(), eventMessage.idempotencyKey());
                return;
            }
        } catch (RuntimeException e) {
            log.warn("Failed to check idempotency key, proceeding (fail-open): workspaceId={}, idempotencyKey={}, error={}",
                eventMessage.workspaceId(), eventMessage.idempotencyKey(), e.getMessage(), e);
        }

        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE,
                RabbitConfig.EVENTS_ROUTING_KEY, eventMessage);
            log.debug("Event published successfully: workspaceId={}, projectId={}",
                eventMessage.workspaceId(), eventMessage.projectId());
        } catch (RuntimeException e) {
            log.error("Failed to publish event: workspaceId={}, projectId={}, error={}",
                eventMessage.workspaceId(), eventMessage.projectId(), e.getMessage(), e);
        }
    }
}
