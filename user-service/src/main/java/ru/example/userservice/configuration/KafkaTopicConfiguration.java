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

    @Bean
    public NewTopic stockReservationResultDltTopic() {
        return TopicBuilder
                .name(KafkaTopics.STOCK_RESERVATION_RESULT_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic stockReleaseResultDltTopic() {
        return TopicBuilder
                .name(KafkaTopics.STOCK_RELEASE_RESULT_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}