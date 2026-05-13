package ru.example.productservice.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.productservice.entity.*;
import ru.example.productservice.model.event.OrderCancellationRequestedEvent;
import ru.example.productservice.model.event.StockReleaseResultEvent;
import ru.example.productservice.repository.ProcessedKafkaEventRepository;
import ru.example.productservice.repository.ProductOutboxEventRepository;
import ru.example.productservice.repository.ProductRepository;
import ru.example.productservice.repository.StockReservationRepository;
import ru.example.productservice.service.StockReleaseSagaService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockReleaseSagaServiceImpl implements StockReleaseSagaService {

    private static final String CONSUMER_NAME = "product-service-order-cancellation-consumer";

    private final ProductRepository productRepository;
    private final StockReservationRepository stockReservationRepository;
    private final ProductOutboxEventRepository productOutboxEventRepository;
    private final ProcessedKafkaEventRepository processedKafkaEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void handleOrderCancellationRequested(
            UUID sourceEventId,
            OrderCancellationRequestedEvent event
    ) {
        if (processedKafkaEventRepository.existsByEventIdAndConsumerName(sourceEventId, CONSUMER_NAME)) {
            return;
        }

        StockReleaseResultEvent result = tryReleaseStock(event);

        ProductOutboxEvent outboxEvent = createStockReleaseResultOutboxEvent(
                event.orderPublicId(),
                result
        );

        productOutboxEventRepository.save(outboxEvent);

        processedKafkaEventRepository.save(
                ProcessedKafkaEvent.builder()
                        .eventId(sourceEventId)
                        .consumerName(CONSUMER_NAME)
                        .build()
        );
    }

    private StockReleaseResultEvent tryReleaseStock(
            OrderCancellationRequestedEvent event
    ) {
        List<StockReservation> reservations =
                stockReservationRepository.findAllByOrderPublicIdAndStatus(
                        event.orderPublicId(),
                        StockReservationStatus.RESERVED
                );

        if (reservations.isEmpty()) {
            return new StockReleaseResultEvent(
                    event.orderPublicId(),
                    false,
                    "Активные резервы для заказа " + event.orderPublicId() + " не найдены"
            );
        }

        Set<String> productPublicIds = reservations.stream()
                .map(StockReservation::getProductPublicId)
                .collect(Collectors.toSet());

        List<Product> products = productRepository.findAllByPublicIdInForUpdate(productPublicIds);

        Map<String, Product> productsByPublicId = products.stream()
                .collect(Collectors.toMap(Product::getPublicId, Function.identity()));

        for (StockReservation reservation : reservations) {
            Product product = productsByPublicId.get(reservation.getProductPublicId());

            if (product == null) {
                return new StockReleaseResultEvent(
                        event.orderPublicId(),
                        false,
                        "Товар productPublicId=" + reservation.getProductPublicId()
                                + " для освобождения резерва не найден"
                );
            }

            if (product.getStockQuantity() == null) {
                return new StockReleaseResultEvent(
                        event.orderPublicId(),
                        false,
                        "У товара «" + product.getName() + "» не задан stockQuantity"
                );
            }
        }

        for (StockReservation reservation : reservations) {
            Product product = productsByPublicId.get(reservation.getProductPublicId());

            product.setStockQuantity(
                    product.getStockQuantity() + reservation.getQuantity()
            );

            reservation.setStatus(StockReservationStatus.RELEASED);
        }

        return new StockReleaseResultEvent(
                event.orderPublicId(),
                true,
                null
        );
    }

    private ProductOutboxEvent createStockReleaseResultOutboxEvent(
            String orderPublicId,
            StockReleaseResultEvent result
    ) {
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Не удалось сериализовать результат освобождения stock для заказа: "
                            + orderPublicId,
                    exception
            );
        }

        return ProductOutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .aggregateType("ORDER")
                .aggregateId(orderPublicId)
                .eventType(result.success()
                        ? "StockReleaseSucceeded"
                        : "StockReleaseFailed")
                .payload(payloadJson)
                .status(OutboxEventStatus.NEW)
                .attempts(0)
                .build();
    }
}