package ru.example.productservice.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import ru.example.productservice.messaging.KafkaTopics;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    public NewTopic stockReservationResultTopic() {
        return TopicBuilder
                .name(KafkaTopics.STOCK_RESERVATION_RESULT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderCancellationRequestedTopic() {
        return TopicBuilder
                .name(KafkaTopics.ORDER_CANCELLATION_REQUESTED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockReleaseResultTopic() {
        return TopicBuilder
                .name(KafkaTopics.STOCK_RELEASE_RESULT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}