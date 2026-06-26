package com.akarengin.pulseforge.processing;

import com.akarengin.pulseforge.ingestion.config.RabbitConfig;
import com.akarengin.pulseforge.ingestion.dto.EventMessage;
import com.akarengin.pulseforge.ingestion.dto.EventRequest;
import com.akarengin.pulseforge.ingestion.service.EventPersistenceService;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final EventPersistenceService eventPersistenceService;

    @RabbitListener(queues = RabbitConfig.EVENTS_QUEUE)
    public void consumeEvent(EventMessage eventMessage, Message message, Channel channel)
        throws IOException {

        try {
            log.info("Consuming event: workspaceId={}, projectId={}, type={}",
                eventMessage.workspaceId(), eventMessage.projectId(), eventMessage.type());

            EventRequest eventRequest = new EventRequest(eventMessage.type(),
                eventMessage.payload(), eventMessage.idempotencyKey());

            eventPersistenceService.persist(eventMessage.workspaceId(), eventMessage.projectId(),
                eventRequest);

            // Manual ACK - acknowledge successful processing
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            log.info("Event processed successfully: type={}", eventMessage.type());

        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate event detected (redelivery): {}, ACKing without requeue", eventMessage.idempotencyKey());

            // Manual ACK - acknowledge successful processing even for duplicates
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

        } catch (IOException | RuntimeException e) {
            log.error("Failed to process event: {}", eventMessage, e);

            // Manual NACK - reject and send to DLQ
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
