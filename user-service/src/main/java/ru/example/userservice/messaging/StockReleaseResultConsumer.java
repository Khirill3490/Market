package ru.example.userservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.example.userservice.messaging.exception.NonRetryableKafkaEventException;
import ru.example.userservice.model.event.StockReleaseResultEvent;
import ru.example.userservice.service.StockReleaseResultService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReleaseResultConsumer {

    private final ObjectMapper objectMapper;
    private final StockReleaseResultService stockReleaseResultService;

    @KafkaListener(
            topics = "${market.kafka.topics.stock-release-result}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handle(
            String payload,
            @Header("eventId") String eventId
    ) {
        UUID parsedEventId = parseEventId(eventId);
        StockReleaseResultEvent event = parsePayload(payload, eventId);

        try {
            stockReleaseResultService.handleStockReleaseResult(
                    parsedEventId,
                    event
            );

            log.info(
                    "Stock release result event handled: eventId={}, orderPublicId={}, success={}",
                    eventId,
                    event.orderPublicId(),
                    event.success()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to process stock release result event: eventId={}, orderPublicId={}, success={}",
                    eventId,
                    event.orderPublicId(),
                    event.success(),
                    exception
            );

            throw new IllegalStateException(
                    "Failed to process stock release result event: " + eventId,
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

    private StockReleaseResultEvent parsePayload(
            String payload,
            String eventId
    ) {
        try {
            return objectMapper.readValue(
                    payload,
                    StockReleaseResultEvent.class
            );
        } catch (JsonProcessingException exception) {
            throw new NonRetryableKafkaEventException(
                    "Failed to deserialize StockReleaseResultEvent: eventId=" + eventId,
                    exception
            );
        }
    }
}