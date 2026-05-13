package ru.example.userservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
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
        try {
            StockReleaseResultEvent event = objectMapper.readValue(
                    payload,
                    StockReleaseResultEvent.class
            );

            stockReleaseResultService.handleStockReleaseResult(
                    UUID.fromString(eventId),
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
                    "Failed to handle stock release result event: eventId={}, payload={}",
                    eventId,
                    payload,
                    exception
            );

            throw new IllegalStateException(
                    "Failed to handle stock release result event: " + eventId,
                    exception
            );
        }
    }
}