package com.akarengin.pulseforge.ingestion.service;

import com.akarengin.pulseforge.ingestion.config.RabbitConfig;
import com.akarengin.pulseforge.ingestion.dto.EventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishEvent(EventMessage eventMessage) {
        log.info("Publishing event to queue: workspaceId={}, type={}", eventMessage.workspaceId(), eventMessage.type());
        
        rabbitTemplate.convertAndSend(RabbitConfig.EVENTS_EXCHANGE, RabbitConfig.EVENTS_ROUTING_KEY, eventMessage);
        
        log.debug("Event published successfully: workspaceId={}, projectId={}", eventMessage.workspaceId(), eventMessage.projectId());
    }
}
