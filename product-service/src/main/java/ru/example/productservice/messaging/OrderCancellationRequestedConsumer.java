package ru.example.productservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.example.productservice.messaging.exception.NonRetryableKafkaEventException;
import ru.example.productservice.model.event.OrderCancellationRequestedEvent;
import ru.example.productservice.service.StockReleaseSagaService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancellationRequestedConsumer {

    private final ObjectMapper objectMapper;
    private final StockReleaseSagaService stockReleaseSagaService;

    @KafkaListener(
            topics = "${market.kafka.topics.order-cancellation-requested}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handle(
            String payload,
            @Header("eventId") String eventId
    ) {
        UUID parsedEventId = parseEventId(eventId);
        OrderCancellationRequestedEvent event = parsePayload(payload, eventId);

        try {
            stockReleaseSagaService.handleOrderCancellationRequested(
                    parsedEventId,
                    event
            );

            log.info(
                    "Order cancellation requested event handled: eventId={}, orderPublicId={}",
                    eventId,
                    event.orderPublicId()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to process order cancellation requested event: eventId={}, orderPublicId={}",
                    eventId,
                    event.orderPublicId(),
                    exception
            );

            throw new IllegalStateException(
                    "Failed to process order cancellation requested event: " + eventId,
                    exception
            );
        }
    }

    private UUID parseEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new NonRetryableKafkaEventException(
                    "Kafka eventId header is missing or blank"
            );
        }

        try {
            return UUID.fromString(eventId);
        } catch (IllegalArgumentException exception) {
            throw new NonRetryableKafkaEventException(
                    "Kafka eventId header is not a valid UUID: " + eventId,
                    exception
            );
        }
    }

    private OrderCancellationRequestedEvent parsePayload(
            String payload,
            String eventId
    ) {
        try {
            return objectMapper.readValue(
                    payload,
                    OrderCancellationRequestedEvent.class
            );
        } catch (JsonProcessingException exception) {
            throw new NonRetryableKafkaEventException(
                    "Failed to deserialize OrderCancellationRequestedEvent: eventId=" + eventId,
                    exception
            );
        }
    }
}