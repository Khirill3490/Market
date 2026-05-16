package ru.example.productservice.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.example.productservice.messaging.exception.NonRetryableKafkaEventException;
import ru.example.productservice.model.event.StockReservationRequestedEvent;
import ru.example.productservice.service.StockReservationSagaService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationRequestedConsumer {

    private final ObjectMapper objectMapper;
    private final StockReservationSagaService stockReservationSagaService;

    @KafkaListener(
            topics = "${market.kafka.topics.stock-reservation-requested}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handle(
            String payload,
            @Header("eventId") String eventId
    ) {
        UUID parsedEventId = parseEventId(eventId);
        StockReservationRequestedEvent event = parsePayload(payload, eventId);

        try {
            stockReservationSagaService.handleStockReservationRequested(
                    parsedEventId,
                    event
            );

            log.info(
                    "Stock reservation requested event handled: eventId={}, orderPublicId={}",
                    eventId,
                    event.orderPublicId()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to process stock reservation requested event: eventId={}, orderPublicId={}",
                    eventId,
                    event.orderPublicId(),
                    exception
            );

            throw new IllegalStateException(
                    "Failed to process stock reservation requested event: " + eventId,
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

    private StockReservationRequestedEvent parsePayload(
            String payload,
            String eventId
    ) {
        try {
            return objectMapper.readValue(
                    payload,
                    StockReservationRequestedEvent.class
            );
        } catch (JsonProcessingException exception) {
            throw new NonRetryableKafkaEventException(
                    "Failed to deserialize StockReservationRequestedEvent: eventId=" + eventId,
                    exception
            );
        }
    }
}