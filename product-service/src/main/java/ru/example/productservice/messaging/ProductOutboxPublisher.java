package ru.example.productservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.example.productservice.entity.OutboxEventStatus;
import ru.example.productservice.entity.ProductOutboxEvent;
import ru.example.productservice.repository.ProductOutboxEventRepository;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOutboxPublisher {

    private final ProductOutboxEventRepository productOutboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${market.outbox.batch-size:50}")
    private int batchSize;

    @Value("${market.outbox.max-attempts:5}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${market.outbox.publish-delay-ms:5000}")
    @Transactional
    public void publishNewEvents() {
        List<ProductOutboxEvent> events = productOutboxEventRepository.findBatchForPublishing(
                OutboxEventStatus.NEW.name(),
                batchSize
        );

        if (events.isEmpty()) {
            return;
        }

        for (ProductOutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(ProductOutboxEvent event) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    resolveTopic(event.getEventType()),
                    event.getAggregateId(),
                    event.getPayload()
            );

            addHeader(record, "eventId", event.getEventId().toString());
            addHeader(record, "eventType", event.getEventType());
            addHeader(record, "aggregateType", event.getAggregateType());
            addHeader(record, "aggregateId", event.getAggregateId());

            kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);

            event.setStatus(OutboxEventStatus.PUBLISHED);
            event.setPublishedAt(OffsetDateTime.now());
            event.setLastError(null);

            log.info(
                    "Product outbox event published: eventId={}, eventType={}, aggregateId={}",
                    event.getEventId(),
                    event.getEventType(),
                    event.getAggregateId()
            );
        } catch (Exception exception) {
            int attempts = event.getAttempts() + 1;

            event.setAttempts(attempts);
            event.setLastError(exception.getMessage());

            if (attempts >= maxAttempts) {
                event.setStatus(OutboxEventStatus.FAILED);
            }

            log.warn(
                    "Failed to publish product outbox event: eventId={}, attempts={}, error={}",
                    event.getEventId(),
                    attempts,
                    exception.getMessage()
            );
        }
    }

    private void addHeader(
            ProducerRecord<String, String> record,
            String name,
            String value
    ) {
        record.headers().add(
                name,
                value.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String resolveTopic(String eventType) {
        return switch (eventType) {
            case "StockReservationSucceeded", "StockReservationFailed" ->
                    KafkaTopics.STOCK_RESERVATION_RESULT;

            case "StockReleaseSucceeded", "StockReleaseFailed" ->
                    KafkaTopics.STOCK_RELEASE_RESULT;

            default ->
                    throw new IllegalArgumentException(
                            "Неизвестный тип product outbox event: " + eventType
                    );
        };
    }
}