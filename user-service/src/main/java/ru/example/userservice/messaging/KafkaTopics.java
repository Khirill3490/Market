package ru.example.userservice.messaging;

public final class KafkaTopics {

    public static final String STOCK_RESERVATION_REQUESTED =
            "market.order.stock-reservation-requested.v1";

    public static final String STOCK_RESERVATION_RESULT =
            "market.product.stock-reservation-result.v1";

    private KafkaTopics() {
    }
}