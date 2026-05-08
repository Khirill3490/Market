package ru.example.productservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.productservice.entity.ProcessedKafkaEvent;

import java.util.UUID;

public interface ProcessedKafkaEventRepository extends JpaRepository<ProcessedKafkaEvent, Long> {

    boolean existsByEventIdAndConsumerName(UUID eventId, String consumerName);
}