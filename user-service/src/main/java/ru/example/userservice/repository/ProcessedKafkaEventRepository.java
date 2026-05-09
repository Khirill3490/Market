package ru.example.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.userservice.entity.ProcessedKafkaEvent;

import java.util.UUID;

public interface ProcessedKafkaEventRepository extends JpaRepository<ProcessedKafkaEvent, Long> {

    boolean existsByEventIdAndConsumerName(UUID eventId, String consumerName);
}