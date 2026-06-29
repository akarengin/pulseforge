package com.akarengin.pulseforge.processing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.akarengin.pulseforge.ingestion.dto.EventMessage;
import com.akarengin.pulseforge.ingestion.service.EventPersistenceService;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

@ExtendWith(MockitoExtension.class)
class EventConsumerTest {

    @Mock
    private EventPersistenceService eventPersistenceService;

    @Mock
    private Channel channel;

    @Mock
    private Message message;

    @Mock
    private MessageProperties messageProperties;

    @InjectMocks
    private EventConsumer eventConsumer;

    @Test
    void consumeEvent_onSuccess_acknowledgesMessage() throws IOException {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        long deliveryTag = 1L;

        EventMessage eventMessage = new EventMessage(
            "user_login",
            Map.of("userId", 123),
            workspaceId,
            projectId,
            "dedup-key-1"
        );

        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(deliveryTag);

        // Act
        eventConsumer.consumeEvent(eventMessage, message, channel);

        // Assert: verify ACK was called (message processed successfully)
        verify(eventPersistenceService).persist(eq(workspaceId), eq(projectId), any());
        verify(channel).basicAck(deliveryTag, false);
    }

    @Test
    void consumeEvent_onException_rejectsMessageToDLQ() throws IOException {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        long deliveryTag = 2L;

        EventMessage eventMessage = new EventMessage(
            "deploy",
            Map.of("service", "api"),
            workspaceId,
            projectId,
            "dedup-key-2"
        );

        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(deliveryTag);

        // Mock exception during processing
        when(eventPersistenceService.persist(eq(workspaceId), eq(projectId), any()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act
        eventConsumer.consumeEvent(eventMessage, message, channel);

        // Assert: verify NACK was called (requeue=false sends to DLQ)
        verify(channel).basicNack(deliveryTag, false, false);
    }

    @Test
    void consumeEvent_onIOException_rejectsMessageToDLQ() throws IOException {
        // Arrange
        UUID workspaceId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        long deliveryTag = 3L;

        EventMessage eventMessage = new EventMessage(
            "error_event",
            Map.of("level", "critical"),
            workspaceId,
            projectId,
            "dedup-key-3"
        );

        when(message.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getDeliveryTag()).thenReturn(deliveryTag);

        // Mock IO exception during ACK
        when(eventPersistenceService.persist(eq(workspaceId), eq(projectId), any()))
            .thenThrow(new RuntimeException("Channel closed unexpectedly"));

        // Act
        eventConsumer.consumeEvent(eventMessage, message, channel);

        // Assert: verify NACK was called to route to DLQ
        verify(channel).basicNack(deliveryTag, false, false);
    }
}


