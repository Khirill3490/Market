package ru.example.userservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ru.example.userservice.model.event.StockReservationResultEvent;
import ru.example.userservice.service.StockReservationResultService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationResultConsumer {

    private final ObjectMapper objectMapper;
    private final StockReservationResultService stockReservationResultService;

    @KafkaListener(
            topics = "${market.kafka.topics.stock-reservation-result}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handle(
            String payload,
            @Header("eventId") String eventId
    ) {
        try {
            StockReservationResultEvent event = objectMapper.readValue(
                    payload,
                    StockReservationResultEvent.class
            );

            stockReservationResultService.handleStockReservationResult(
                    UUID.fromString(eventId),
                    event
            );

            log.info(
                    "Stock reservation result event handled: eventId={}, orderPublicId={}, success={}",
                    eventId,
                    event.orderPublicId(),
                    event.success()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to handle stock reservation result event: eventId={}, payload={}",
                    eventId,
                    payload,
                    exception
            );

            throw new IllegalStateException(
                    "Failed to handle stock reservation result event: " + eventId,
                    exception
            );
        }
    }
}