package ru.example.userservice.messaging;

public final class KafkaTopics {

    public static final String STOCK_RESERVATION_REQUESTED =
            "market.order.stock-reservation-requested.v1";

    public static final String STOCK_RESERVATION_RESULT =
            "market.product.stock-reservation-result.v1";

    public static final String ORDER_CANCELLATION_REQUESTED =
            "market.order.cancellation-requested.v1";

    public static final String STOCK_RELEASE_RESULT =
            "market.product.stock-release-result.v1";

    public static final String STOCK_RESERVATION_RESULT_DLT =
            STOCK_RESERVATION_RESULT + ".dlt";

    public static final String STOCK_RELEASE_RESULT_DLT =
            STOCK_RELEASE_RESULT + ".dlt";

    private KafkaTopics() {
    }
}