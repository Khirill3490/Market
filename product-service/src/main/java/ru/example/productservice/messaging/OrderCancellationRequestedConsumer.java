package ru.example.productservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
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
        try {
            OrderCancellationRequestedEvent event = objectMapper.readValue(
                    payload,
                    OrderCancellationRequestedEvent.class
            );

            stockReleaseSagaService.handleOrderCancellationRequested(
                    UUID.fromString(eventId),
                    event
            );

            log.info(
                    "Order cancellation requested event handled: eventId={}, orderPublicId={}",
                    eventId,
                    event.orderPublicId()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to handle order cancellation requested event: eventId={}, payload={}",
                    eventId,
                    payload,
                    exception
            );

            throw new IllegalStateException(
                    "Failed to handle order cancellation requested event: " + eventId,
                    exception
            );
        }
    }
}