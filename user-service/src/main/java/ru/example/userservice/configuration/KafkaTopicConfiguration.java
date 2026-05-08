package ru.example.userservice.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import ru.example.userservice.messaging.KafkaTopics;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic stockReservationRequestedTopic() {
        return TopicBuilder
                .name(KafkaTopics.STOCK_RESERVATION_REQUESTED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}