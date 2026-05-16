package ru.example.productservice.messaging.exception;

public class NonRetryableKafkaEventException extends RuntimeException {

    public NonRetryableKafkaEventException(String message) {
        super(message);
    }

    public NonRetryableKafkaEventException(String message, Throwable cause) {
        super(message, cause);
    }
}