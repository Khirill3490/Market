package ru.example.productservice.messaging;

public final class KafkaTopics {

    public static final String STOCK_RESERVATION_RESULT =
            "market.product.stock-reservation-result.v1";

    public static final String ORDER_CANCELLATION_REQUESTED =
            "market.order.cancellation-requested.v1";

    public static final String STOCK_RELEASE_RESULT =
            "market.product.stock-release-result.v1";

    private KafkaTopics() {
    }
}