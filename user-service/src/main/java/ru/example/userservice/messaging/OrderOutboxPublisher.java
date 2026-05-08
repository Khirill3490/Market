package ru.example.userservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.example.userservice.entity.OrderOutboxEvent;
import ru.example.userservice.entity.OutboxEventStatus;
import ru.example.userservice.repository.OrderOutboxEventRepository;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOutboxPublisher {

    private final OrderOutboxEventRepository orderOutboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${market.outbox.batch-size:50}")
    private int batchSize;

    @Value("${market.outbox.max-attempts:5}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${market.outbox.publish-delay-ms:5000}")
    @Transactional
    public void publishNewEvents() {
        List<OrderOutboxEvent> events = orderOutboxEventRepository.findBatchForPublishing(
                OutboxEventStatus.NEW.name(),
                batchSize
        );

        if (events.isEmpty()) {
            return;
        }

        for (OrderOutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OrderOutboxEvent event) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    KafkaTopics.STOCK_RESERVATION_REQUESTED,
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
                    "Outbox событие опубликовано: eventId={}, eventType={}, aggregateId={}",
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
                    "Ошибка публикации события в outbox: eventId={}, attempts={}, error={}",
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
}