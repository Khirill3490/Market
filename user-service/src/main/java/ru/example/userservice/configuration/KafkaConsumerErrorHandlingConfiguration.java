package ru.example.userservice.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import ru.example.userservice.messaging.exception.NonRetryableKafkaEventException;

@Slf4j
@Configuration
public class KafkaConsumerErrorHandlingConfiguration {

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> {
                    String dltTopic = record.topic() + ".dlt";

                    log.error(
                            "Kafka record exhausted retries and will be published to DLT: sourceTopic={}, dltTopic={}, partition={}, offset={}",
                            record.topic(),
                            dltTopic,
                            record.partition(),
                            record.offset(),
                            exception
                    );

                    return new TopicPartition(dltTopic, record.partition());
                }
        );
    }

    @Bean
    public CommonErrorHandler kafkaCommonErrorHandler(
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer
    ) {
        FixedBackOff fixedBackOff = new FixedBackOff(
                1_000L,
                3L
        );

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                deadLetterPublishingRecoverer,
                fixedBackOff
        );

        errorHandler.addNotRetryableExceptions(
                NonRetryableKafkaEventException.class
        );

        return errorHandler;
    }
}