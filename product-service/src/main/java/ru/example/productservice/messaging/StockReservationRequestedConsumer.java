package ru.example.productservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
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
        try {
            StockReservationRequestedEvent event = objectMapper.readValue(
                    payload,
                    StockReservationRequestedEvent.class
            );

            stockReservationSagaService.handleStockReservationRequested(
                    UUID.fromString(eventId),
                    event
            );

            log.info(
                    "Stock reservation requested event handled: eventId={}, orderPublicId={}",
                    eventId,
                    event.orderPublicId()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to handle stock reservation requested event: eventId={}, payload={}",
                    eventId,
                    payload,
                    exception
            );

            throw new IllegalStateException(
                    "Failed to handle stock reservation requested event: " + eventId,
                    exception
            );
        }
    }
}